const R = require('ramda')
const _ = require('lodash')
const e1 = 'required fields missing'
const e2 = 'contains illegal fields'
const { logE, logD } = require('../utils')

class StatusError extends Error {
  constructor(statusCode, message) {
    super()
    this.statusCode = statusCode
    this.message = message
  }
}

exports.StatusError = StatusError

const userModel = {
  required: [
    {
      name: 'name',
      validator: el => _.isString(el) && el.length < 100,
    },
    {
      name: 'email',
      validator: el => _.isString(el) && /[\w_\d]+@[\w\d_]+\.[\w]+/.test(el),
    },
    {
      name: 'id',
      validator: el => _.isString(el),
    },
  ],
  fields: [
    {
      name: 'groups',
      validator: el => _.isArray(el),
    },
    {
      name: 'msgToken',
      validator: el => _.isString(el),
    },
    {
      name: 'photoUrl',
      validator: el => _.isString(el),
    },
    {
      name: 'favorites',
      validator: el => _.isArray(el) || _.isString(el),
    },
  ],
}

const projectModel = {
  required: [
    {
      name: 'name',
      validator: el => _.isString(el) && el.length < 30,
    },
    {
      name: 'description',
      validator: el => _.isString(el) && el.length < 100,
    },
    {
      name: 'type',
      validator: el => ['Group', 'Personal'].some(v => v === el),
    },
    {
      name: 'admin',
      validator: el => _.isString(el),
    },
  ],
  fields: [
    {
      name: 'keywords',
      validator: el => {
        if (_.isArray(el) === false) return false
        else if (el.length > 3) return false
        return true
      },
    },
    {
      name: 'deadline',
      validator: el =>
        _.isString(el) &&
        /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+([+-][0-2]\d:[0-5]\d|Z)/.test(
          el,
        ),
    },
    {
      name: 'members',
      validator: el => _.isArray(el),
    },
    {
      name: 'favoriteOf',
      validator: el => _.isArray(el) || _.isString(el),
    },
  ],
}

const taskModel = {
  required: [
    {
      name: 'description',
      validator: el => _.isString(el),
    },
    {
      name: 'status',
      validator: el => ['ongoing', 'pending', 'completed'].some(x => x === el),
    },
    {
      name: 'name',
      validator: el => _.isString(el),
    },
    ,
  ],
  fields: [
    // {
    //   name: 'assigned_to',
    //   validator: el => _.isArray(el),
    // },
    {
      name: 'deadline',
      validator: el =>
        _.isString(el) &&
        /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+([+-][0-2]\d:[0-5]\d|Z)/.test(
          el,
        ),
    },
  ],
}

const picker = model => data => {
  const { required, fields } = model
  const allProps = required.concat(fields)

  const illegal = allProps.filter(({ name, validator }) => {
    const v = data[name]
    if (v === undefined) return false
    else if (validator(v) === true) return false
    else return true
  })

  if (illegal.length > 0) {
    logE(illegal.map(R.prop('name')))
    throw new StatusError(500, e2)
  } else {
    const allPropNames = allProps.map(R.prop('name'))
    return R.pick(allPropNames)(data)
  }
}

const validator = model => data => {
  const { required } = model
  const missing = required.filter(({ name }) => data[name] === undefined)
  if (missing.length > 0) {
    console.log(missing)
    throw new StatusError(500, e1)
  }

  return picker(model)(data)
}

exports.projectPicker = picker(projectModel)
exports.userPicker = picker(userModel)
exports.taskPicker = picker(taskModel)

exports.projectValidator = validator(projectModel)
exports.userValidator = validator(userModel)
exports.taskValidator = validator(taskModel)
