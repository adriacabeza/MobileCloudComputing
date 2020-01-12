const { tmpdir } = require('os')
const { dirname, join } = require('path')
const sharp = require('sharp')
const fs = require('fs-extra')
const mkdirp = require('mkdirp-promise')

const { Storage } = require('@google-cloud/storage')
const gcs = new Storage()

const handler = async function(object) {
  const bucket = gcs.bucket(object.bucket)

  const { name: filePath } = object
  const fileName = filePath.split('/').pop()

  const bucketDir = dirname(filePath)

  const workingDir = join(tmpdir(), 'resize')
  await mkdirp(workingDir)

  const tmpFilePath = join(workingDir, fileName)
  console.log(tmpFilePath)

  const marker = '@s_'

  if (fileName.includes(marker) || !object.contentType.includes('image')) {
    return false
  }

  if (fileName.length > 30) return

  await bucket.file(filePath).download({
    destination: tmpFilePath,
  })

  // const widths = [480, 960]

  const sizes = [
    {
      width: 480,
      height: 640,
    },
    {
      width: 960,
      height: 1280,
    },
  ]

  const tasks = sizes.map(async opt => {
    const ext = fileName.split('.').pop()
    const imgName = fileName.replace(`.${ext}`, '')
    const newImgName = `${imgName}${marker}${opt.width}.${ext}`
    const imgPath = join(workingDir, newImgName)

    await sharp(tmpFilePath)
      .resize(opt)
      .toFile(imgPath)

    return bucket.upload(imgPath, {
      destination: join(bucketDir, newImgName),
    })
  })

  await Promise.all(tasks)

  return fs.remove(workingDir)
}

module.exports = handler
