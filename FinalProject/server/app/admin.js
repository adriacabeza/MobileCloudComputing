const admin = require('firebase-admin')

const { snapToList } = require('./utils')

const serviceAccount = require('./firebase-adminsdk.json')

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://mcc-fall-2019-g10.firebaseio.com',
})

const store = admin.firestore()

exports.admin = admin

const { StatusError } = require('./fn/validators')
exports.notExistsThenThrow = collection => async doc => {
  const snap = await collection.doc(doc).get()
  if (snap.exists === false) {
    throw new StatusError(404, `${doc} does not exist`)
  }
  return snap.data()
}

exports.existsThenThrow = collection => async doc => {
  const snap = await collection.doc(doc).get()
  if (snap.exists === true) {
    throw new StatusError(500, `id ${doc} already exists`)
  }
  return snap.data()
}

exports.notUniqThenThrow = collection => async ({ prop, value }) => {
  console.log(prop, value)
  const snap = await collection.where(prop, '==', value).get()
  const data = snapToList(snap)
  if (data.length > 0) {
    throw new StatusError(500, `${prop} must be unique`)
  }
  return data
}

exports.storeRef = store
exports.userStore = store.collection('users')
exports.projectStore = store.collection('projects')
exports.arrayUnion = admin.firestore.FieldValue.arrayUnion.bind(
  admin.firestore.FieldValue,
)

exports.arrayRemove = admin.firestore.FieldValue.arrayRemove.bind(
  admin.firestore.FieldValue,
)
