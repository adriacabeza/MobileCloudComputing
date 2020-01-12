const { get } = require('lodash')

exports.errorHandler = (err, req, res) => {
  console.log(err)
  const statusCode = get(err, 'statusCode', 500)
  const message = get(err, 'message', 'Internal Error')
  console.error('errorHandler:', err.message)
  res.status(statusCode).json({
    statusCode,
    message,
  })
}
