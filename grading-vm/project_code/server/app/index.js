const express = require('express')
const app = express()
const port = process.env.PORT || 5000
const bodyParser = require('body-parser')
const cors = require('cors')({ origin: true })

app.use(bodyParser.urlencoded({ extended: false }))
app.use(bodyParser.json())
app.use(cors)

app.use('/api/project', require('./fn/project.functions'))
app.use('/api/user', require('./fn/user.functions'))

app.listen(port, () => console.log(`App listening on port ${port}!`))
