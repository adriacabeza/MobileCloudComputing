const userRouter = require('../RouterFactory')()
const R = require('ramda')
const {
  existsThenThrow,
  userStore: store,
  notExistsThenThrow,
  notUniqThenThrow,
  projectStore,
  arrayRemove,
  arrayUnion,
} = require('../admin')
const { getProject } = require('./project.db')
const { userValidator, userPicker } = require('./validators')
const { snapToList } = require('../utils')
const { errorHandler } = require('./error.middleware')

userRouter
  .get('/all', async (req, res) => {
    let snaps = await store.get()

    res.json({
      all: snapToList(snaps).map(R.pick(['name', 'id'])),
    })
  })
  .get('/search', async (req, res) => {
    const { pre } = req.query
    if (pre === undefined) {
      const snap = await store.get()
      res.json(snapToList(snap))
    }
    const snap = await store
      .where('name', '==', pre)
      .limit(5)
      .get()
    const users = snapToList(snap)
    res.json(users)
  })
  .post('/:id/meta/keywords', async (req, res) => {
    let { id } = req.params
    try {
      const snap = await projectStore
        .where('members', 'array-contains', id)
        .get()

      const keyWordsList = R.flatten(snapToList(snap).map(el => el.keywords))
      const keyWordsSet = new Set(keyWordsList)
      console.log(keyWordsSet)

      res.json(Array.from(keyWordsSet))
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .put('/:uid/favorites/:pid', async (req, res) => {
    try {
      const {
        params: { uid, pid },
      } = req

      await notExistsThenThrow(store)(uid)

      await notExistsThenThrow(projectStore)(pid)

      await Promise.all([
        store.doc(uid).update({
          favorites: arrayUnion(pid),
        }),
        projectStore.doc(pid).update({
          favoriteOf: arrayUnion(uid),
        }),
      ])

      res.json({ uid, pid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/:uid/favorites/:pid', async (req, res) => {
    try {
      const {
        params: { uid, pid },
      } = req

      await notExistsThenThrow(store)(uid)

      await notExistsThenThrow(projectStore)(pid)

      await Promise.all([
        store.doc(uid).update({
          favorites: arrayRemove(pid),
        }),
        projectStore.doc(pid).update({
          favoriteOf: arrayRemove(uid),
        }),
      ])

      res.json({ uid, pid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .post('/:id/search', async (req, res) => {
    let { byKeyword, byName, on, fromStringStart } = req.body
    const { id } = req.params

    if (fromStringStart !== true) {
      fromStringStart = false
    }

    if (on !== 'projects') throw new StatusError(404, 'not allowed')

    try {
      const data = await notExistsThenThrow(store)(id)
      const { projects } = data
      const gets = projects.map(pid => getProject(pid))
      const projectData = await Promise.all(gets)

      let filtered

      if (byKeyword !== undefined) {
        filtered = projectData.filter(({ keywords }) =>
          keywords.includes(byKeyword),
        )
      } else if (byName !== undefined) {
        const rgx =
          fromStringStart === true
            ? new RegExp(`^${byName}`, 'i')
            : new RegExp(`${byName}`, 'i')

        filtered = projectData.filter(({ name }) => rgx.test(name))
      } else {
        throw new StatusError(404, 'no query provided')
      }
      res.json(filtered)
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .get('/p/:id', async (req, res) => {
    const { id } = req.params
    try {
      const data = await notExistsThenThrow(store)(id)
      const { projects } = data
      const gets = projects.map(pid => getProject(pid))
      let projectData = await Promise.all(gets)

      projectData = projectData.sort((a, b) => {
        return new Date(a.updated) - new Date(b.updated)
      })

      res.json({
        ...data,
        projects: projectData,
      })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .get('/:id', async (req, res) => {
    const { id } = req.params
    try {
      const data = await notExistsThenThrow(store)(id)
      res.json(data)
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .post('/', async (req, res) => {
    const { body } = req
    try {
      const validated = userValidator(body)
      const { name, id } = validated
      await Promise.all([
        existsThenThrow(store)(id),
        notUniqThenThrow(store)({ prop: 'name', value: name }),
      ])
      await store.doc(id).set({
        ...validated,
        projects: [],
        favorites: [],
        assigned_tasks: [],
      })
      res.json({ id })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/:id', async (req, res) => {
    const { id } = req.params
    await store.doc(id).delete()
    res.json({ id })
  })
  .put('/:id', async (req, res) => {
    const {
      params: { id },
      body,
    } = req
    try {
      const validated = userPicker(body)
      await store.doc(id).update(validated)
      res.json({ id })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })

userRouter.all('/*', (req, res) => {
  res.status(404).json({
    message: 'oh-ho',
  })
})

userRouter.use(errorHandler)
module.exports = userRouter
