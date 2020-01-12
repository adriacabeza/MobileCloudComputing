const endpoint = 'http://localhost:9999/mcc-fall-2019-g10/us-central1/userfs'

const axios = require('axios').default

module.exports.get = async id => {
  const { data } = await axios.get(`${endpoint}/id`)
  return data
}

module.exports.post = async uid => {
  const { data } = await axios.post(`${endpoint}`, {
    name: uid,
    email: 'test@test.test',
    id: uid || '2',
  })
  return data
}

module.exports.put = async () => {
  const { data } = await axios.put(`${endpoint}/test`, {
    name: 'test_put',
  })
  return data
}

module.exports._delete = async () => {
  const { data } = await axios.delete(`${endpoint}/test`)
  return data
}
