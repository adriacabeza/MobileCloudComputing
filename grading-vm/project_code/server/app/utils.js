exports.snapToList = snap => {
  const list = []
  snap.forEach(e => {
    list.push(e.data())
  })
  return list
}

const chalk = require('chalk')
exports.logE = (...msg) => console.error(chalk.red(msg))
exports.logD = (...msg) => console.debug(chalk.blue(msg))
