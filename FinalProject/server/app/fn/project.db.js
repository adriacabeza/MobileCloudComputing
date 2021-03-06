const { projectStore: store, notExistsThenThrow } = require('../admin')

const { snapToList } = require('../utils')

const getProject = async id => {
  const data = await notExistsThenThrow(store)(id)
  const _tasksSnap = await store
    .doc(id)
    .collection('tasks')
    .orderBy('updated')
    .get()
  const tasks = snapToList(_tasksSnap)

  const _attachmentsSnap = await store
    .doc(id)
    .collection('attachments')
    .orderBy('updated')
    .get()
  const attachments = snapToList(_attachmentsSnap)

  return {
    ...data,
    tasks,
    attachments,
  }
}

exports.getProject = getProject
