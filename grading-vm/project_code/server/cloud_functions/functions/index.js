const functions = require('firebase-functions')
const { admin, userStore, notExistsThenThrow } = require('./admin')
const { difference } = require('lodash')

// exports.userfs = functions
//   .runWith({ memory: '1GB' })
//   .https.onRequest(require('./fn/user.functions'))
// exports.projectfs = functions
//   .runWith({ memory: '1GB' })
//   .https.onRequest(require('./fn/project.functions'))
exports.pdf = functions
  .runWith({ memory: '1GB' })
  .https.onRequest(require('./fn/pdf.functions'))

exports.resizeImages = functions
  .runWith({
    memory: '2GB',
    timeoutSeconds: 120,
  })
  .storage.object()
  .onFinalize(require('./image_resize.functions'))

exports.pushNotification = functions
  .runWith({
    memory: '1GB',
    timeoutSeconds: 120,
  })
  .firestore.document('users/{userId}')
  .onUpdate(async (change, context) => {
    try {
      const userId = context.params.userId
      console.log('TCL: userId', userId)

      const { assigned_tasks: old_tasks } = change.before.data()
      console.log('TCL: old_tasks', old_tasks)
      const { assigned_tasks: new_tasks, msgToken } = change.after.data()
      console.log('TCL: msgToken', msgToken)
      const diff = difference(old_tasks, new_tasks)
      if (diff.length > 0) {
        const message = {
          data: {
            diff,
          },
          token: msgToken,
        }

        await admin.messaging().send(message)
      }
    } catch (e) {
      console.log(e)
    }
  })

exports.pushNotificationProject = functions
  .runWith({
    memory: '1GB',
    timeoutSeconds: 120,
  })
  .firestore.document('projects/{projectId}')
  .onUpdate(async (change, context) => {
    try {
      const { projectId } = context.params
      console.log('TCL: projectId ', projectId)

      // const { members: old_members } = change.before.data()
      const { admin: adminId } = change.before.data()
      console.log('TCL: adminId', adminId)
      const { msgToken } = await notExistsThenThrow(userStore)(adminId)
      console.log('TCL: msgToken', msgToken)

      const message = {
        notification: {
          title: 'hej up 1.43% on the day',
          body:
            'hej gained 11.80 points to close at 835.67, up 1.43% on the day.',
        },
        token: msgToken,
      }

      const response = await admin.messaging().send(message)
      console.log('TCL: response', response)
    } catch (e) {
      console.log(e)
    }
  })
