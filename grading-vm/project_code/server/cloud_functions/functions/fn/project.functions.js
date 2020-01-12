const projectRouter = require('../RouterFactory')()
const {
  projectStore: store,
  userStore,
  arrayUnion,
  arrayRemove,
  notExistsThenThrow,
  admin,
} = require('../admin')
const short = require('short-uuid')
const {
  projectValidator,
  projectPicker,
  taskValidator,
  taskPicker,
} = require('./validators')
const { errorHandler } = require('./error.middleware')
const { getProject } = require('./project.db')
const firebase_tools = require('firebase-tools')


const { exec } = require('child-process-promise')

const token =
  '1//0culG_dZhhxSOCgYIARAAGAwSNgF-L9IrcYuk1AptvCeaPceKXWlGvfQdDhyln8zgOAjs0cx_KVS4EnD7bYcBLLsO01klioi8Lg'

const cmdMaker = pid =>
  `gsutil rm -r gs://mcc-fall-2019-g10.appspot.com/projects/${pid}`

const check = ({ uid, pid }) =>
  Promise.all([
    notExistsThenThrow(store)(pid),
    notExistsThenThrow(userStore)(uid),
  ])

projectRouter
  .get('/:pid/tasks/:tid', async (req, res) => {
    const {
      params: { pid, tid },
    } = req
    try {
      await notExistsThenThrow(store)(pid)
      const data = await notExistsThenThrow(store.doc(pid).collection('tasks'))(
        tid,
      )
      res.json(data)
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .get('/:id', async (req, res) => {
    const { id } = req.params
    try {
      const data = await getProject(id)
      res.json(data)
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .post('/:pid/attachments', async (req, res) => {
    const {
      params: { pid },
      body: urlList,
    } = req
    try {
      await notExistsThenThrow(store)(pid)

      urlList.forEach(v => {
        v.id = short.generate()
        v.created = new Date().toISOString()
        v.updated = new Date().toISOString()
      })

      const addAttachment = attachment =>
        store
          .doc(pid)
          .collection('attachments')
          .doc(attachment.id)
          .set(attachment)

      await Promise.all(urlList.map(addAttachment))

      res.json({
        aidList: urlList.map(el => el.id),
      })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .put('/:pid/tasks/:tid/assigned_to/:uid', async (req, res) => {
    const {
      params: { pid, tid, uid },
    } = req
    try {
      await notExistsThenThrow(store)(pid)
      const _tasksRef = store.doc(pid).collection('tasks')
      await notExistsThenThrow(_tasksRef)(tid)

      await Promise.all([
        _tasksRef.doc(tid).update({
          assigned_to: arrayUnion(uid),
          updated: new Date().toISOString(),
        }),
        userStore.doc(uid).update({
          assigned_tasks: arrayUnion(`${pid}/${tid}`),
        }),
      ])

      res.json({ pid, tid, uid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/:pid/tasks/:tid/assigned_to/:uid', async (req, res) => {
    const {
      params: { pid, tid, uid },
    } = req
    try {
      await notExistsThenThrow(store)(pid)
      const _tasksRef = store.doc(pid).collection('tasks')
      await notExistsThenThrow(_tasksRef)(tid)

      await Promise.all([
        _tasksRef.doc(tid).update({
          assigned_to: arrayRemove(uid),
          updated: new Date().toISOString(),
        }),
        userStore.doc(uid).update({
          assigned_tasks: arrayRemove(`${pid}/${tid}`),
        }),
      ])

      res.json({ pid, tid, uid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .put('/:pid/tasks/:tid', async (req, res) => {
    const {
      body: update,
      params: { pid, tid },
    } = req
    try {
      const validated = taskPicker(update)

      await notExistsThenThrow(store)(pid)
      const _tasksRef = store.doc(pid).collection('tasks')
      await notExistsThenThrow(_tasksRef)(tid)

      await _tasksRef.doc(tid).update({
        ...validated,
        updated: new Date().toISOString(),
      })

      res.json({ pid, tid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .post('/:pid/tasks', async (req, res) => {
    const {
      body: tasksList,
      params: { pid },
    } = req
    try {
      await notExistsThenThrow(store)(pid)

      const validated = tasksList.map(taskValidator)

      // attach a id for that
      validated.forEach(v => {
        v.id = short.generate()
        v.created = new Date().toISOString()
        v.updated = new Date().toISOString()
        v.assigned_to = []
      })

      const addTask = taskObject =>
        store
          .doc(pid)
          .collection('tasks')
          .doc(taskObject.id)
          .set(taskObject)

      await Promise.all(validated.map(addTask))

      res.json({ pid, tidList: validated.map(el => el.id) })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/:pid/tasks', async (req, res) => {
    const {
      body: tidList,
      params: { pid },
    } = req
    try {
      await notExistsThenThrow(store)(pid)
      const deleteTask = tid =>
        store
          .doc(pid)
          .collection('tasks')
          .doc(tid)
          .delete()

      await Promise.all(tidList.map(deleteTask))

      res.json({ pid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/:pid/attachments/:tid', async (req, res) => {
    const {
      params: { pid, tid },
    } = req
    try {
      await notExistsThenThrow(store)(pid)
      await store
        .doc(pid)
        .collection('attachments')
        .doc(tid)
        .delete()

      res.json({ pid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/:pid/tasks/:tid', async (req, res) => {
    const {
      params: { pid, tid },
    } = req
    try {
      await notExistsThenThrow(store)(pid)
      await store
        .doc(pid)
        .collection('tasks')
        .doc(tid)
        .delete()

      res.json({ pid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .put('/:pid/members/:uid', async (req, res) => {
    const { uid, pid } = req.params
    try {
      await check({ uid, pid })
      await Promise.all([
        store.doc(pid).update({ members: arrayUnion(uid) }),
        userStore.doc(uid).update({
          projects: arrayUnion(pid),
        }),
      ])
      res.json({ pid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .put('/:pid/members', async (req, res) => {
    const {
      body: midList,
      params: { pid },
    } = req
    try {
      await notExistsThenThrow(store)(pid)

      // check if users are valid
      await Promise.all(midList.map(mid => notExistsThenThrow(userStore)(mid)))

      const updateUser = uid =>
        userStore.doc(uid).update({
          projects: arrayUnion(pid),
        })

      await Promise.all([
        midList.map(mid => updateUser(mid)),
        store.doc(pid).update({ members: arrayUnion(...midList) }),
      ])

      res.json({ pid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/:pid/members/:uid', async (req, res) => {
    const { uid, pid } = req.params
    try {
      await check({ uid, pid })
      await Promise.all([
        userStore.doc(uid).update({
          projects: arrayRemove(pid),
        }),
        store.doc(pid).update({ members: arrayRemove(uid) }),
      ])
      res.json({ pid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/:pid/members', async (req, res) => {
    const {
      body: midList,
      params: { pid },
    } = req
    try {
      await notExistsThenThrow(store)(pid)
      // check if users are valid
      await Promise.all(midList.map(mid => notExistsThenThrow(userStore)(mid)))

      const updateUser = uid =>
        userStore.doc(uid).update({
          projects: arrayRemove(pid),
        })

      await Promise.all([
        midList.map(mid => updateUser(mid)),
        store.doc(pid).update({ members: arrayRemove(...midList) }),
      ])

      res.json({ pid })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .post('/', async (req, res) => {
    const { body } = req
    const id = short.generate()
    try {
      const validated = projectValidator(body)
      const { admin, keywords, members } = validated
      await notExistsThenThrow(userStore)(admin)
      await Promise.all([
        store.doc(id).set({
          ...validated,
          id,
          created: new Date().toISOString(),
          updated: new Date().toISOString(),
          keywords: keywords === undefined ? [] : keywords,
          members: members === undefined ? [admin] : [...members, admin],
          favoriteOf: [],
        }),
        userStore.doc(admin).update({
          projects: arrayUnion(id),
        }),
      ])

      res.json({ id })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/t/:id', async (req, res) => {
    const { id } = req.params
    try {
      const cmd = cmdMaker(id)
      console.log(111, cmd)
      const { stdout } = await exec(cmd)

      res.json(stdout)
    } catch (e) {
      errorHandler(e, req, res)
    }
  })
  .delete('/:id', async (req, res) => {
    const { id } = req.params
    try {
      const { members } = (await store.doc(id).get()).data()
      // console.log(members)

      const removeProjectFromUser = uid =>
        userStore.doc(uid).update({
          projects: arrayRemove(id),
        })

      // console.log(process.env.GCLOUD_PROJECT)
      // console.log(`projects/${id}`)

      await Promise.all([
        ...members.map(removeProjectFromUser),
        await firebase_tools.firestore.delete(`projects/${id}`, {
          project: process.env.GCLOUD_PROJECT,
          recursive: true,
          yes: true,
          token,
        }),
      ])
      res.json({ id })
    } catch (e) {
      errorHandler(e, req, res)
    }

    // delete storage
    const cmd = cmdMaker(id)
    await exec(cmd)
    console.log('deleted storage ', id)
  })
  .put('/:id', async (req, res) => {
    const {
      params: { id },
      body,
    } = req
    try {
      notExistsThenThrow(store)(id)
      const validated = projectPicker(body)
      await store
        .doc(id)
        .update({ ...validated, updated: new Date().toISOString() })
      res.json({ id })
    } catch (e) {
      errorHandler(e, req, res)
    }
  })

projectRouter.all('/*', (req, res) => {
  res.status(404).json({
    message: 'oh-ho',
  })
})

projectRouter.use(errorHandler)

module.exports = projectRouter
