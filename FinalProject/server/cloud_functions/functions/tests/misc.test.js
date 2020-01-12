const endpoint = 'http://localhost:9999/mcc-fall-2019-g10/us-central1/projectfs'

const axios = require('axios').default

const addMember = pid => axios.put(`${endpoint}/${pid}/members/4`)

const create = id =>
  axios.post(endpoint, {
    description: 'test',
    name: `test${id}`,
    admin: '3',
    type: 'Group',
    deadline: '2019-11-10T13:18:15.696Z',
    members: [],
  })

const get = pid => axios.get(`${endpoint}/${pid}`)
// const deleteMember = () => axios.delete(memberAPI)

const { post } = require('./user.crud')

const mem = res => res.data.members

it('first created a new user with id 3', async () => {
  const { id: uid } = await post('3')
  expect(uid).toBe('3')
})

it('first created a new user with id 4', async () => {
  const { id: uid } = await post('4')
  expect(uid).toBe('4')
})

it('then created a new project with admin 3 and add 4 as member', async () => {
  const { data } = await create()
  const { id: pid } = data
  console.log(pid)
  expect(pid).toBeDefined()
  console.log('pid', pid)
  const { data: res } = await addMember(pid)
  expect(res.pid).toBeDefined()
})
