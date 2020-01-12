const { get, post, put, _delete } = require('./user.crud')

it('user post method', async () => {
  const data = await post('test')
  expect(data).toEqual({
    id: 'test',
  })
})

it('user get method', async () => {
  const data = await get('test')
  expect(data).toEqual({
    name: 'test',
    id: 'test',
    email: 'test@test.test',
    projects: [],
  })
})

it('user put method', async () => {
  const data = await put()
  expect(data).toEqual({
    id: 'test',
  })
})

it('user delete method', async () => {
  const data = await _delete()
  expect(data).toEqual({
    id: 'test',
  })
})
