// const cookieParser = require('cookie-parser')()
// const cors = require('cors')({ origin: true })
// const bodyParser = require('body-parser')
const Router = require('express').Router

function Factory() {
  const router = Router()
  // router.use(bodyParser.urlencoded({ extended: false }))
  // router.use(cors)
  // router.use(cookieParser)
  return router
}

module.exports = Factory
