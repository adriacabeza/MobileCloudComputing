// const fs = require('fs')
const pdf = require('html-pdf')

const pdfRouter = require('../RouterFactory')()
const { projectStore, notExistsThenThrow } = require('../admin')
const { StatusError } = require('./validators')

const { errorHandler } = require('./error.middleware')
const html = require('./pdf.template')

pdfRouter.get('/:pid', async (req, res) => {
  // const html = fs.readFileSync('./hello.html', 'utf8')

  const {
    params: { pid },
  } = req

  await notExistsThenThrow(projectStore)(pid)

  const htmlString = await html(pid)
  try {
    // res.send(htmlString)
    pdf.create(htmlString).toStream(function(err, stream) {
      if (err) {
        throw new StatusError(500, 'internal error ')
      }

      res.setHeader('Content-Type', 'application/pdf')
      stream.pipe(res)
    })
  } catch (error) {
    errorHandler(e, req, res)
  }
})

pdfRouter.all('/*', (req, res) => {
  res.status(404).json({
    message: 'oh-ho',
  })
})

module.exports = pdfRouter
