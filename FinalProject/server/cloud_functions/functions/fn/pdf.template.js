const { getProject } = require('./project.db')

const template = data => {
  const {
    tasks,
    attachments,
    name,
    description,
    type,
    updated,
    deadline,
    keywords,
    members,
  } = data

  return `
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Document</title>
</head>

<body>

    <h1>Project report of ${name}</h1>


    <h3>Type</h3>
    <p>${type}</p>

    <h3>Deadline </h3>
    <p>${deadline}</p>

    <h3>Description</h3>
    <p>${description}</p>

    <h3>Members </h3>
    <p>${members.map(m => `<span class="member">${m}</span>`)}</p>

    <h3>Keywords </h3>
    <p>${keywords.map(kw => `<span class="keyword">${kw}</span>`)}</p>

    <h3>Tasks </h3>
    <p>Total ${tasks.length} tasks</p>

    <h3>Attachments </h3>
    <p>Total ${attachments.length} tasks</p>

    <footer>
      <p>
        The project data is last updated at ${updated}
      </p>
      <p>
        This report is generated at ${new Date().toISOString()}
      </p>
    </footer>


</body>

<style>
body {
  width: 100vw;
  height: 100vh;
  padding: 25px;

}
footer{
  color: gray;
  font-size: 75%;
}
</style>

</html>
`
}

async function html(projectId) {
  const data = await getProject(projectId)

  return template(data)
}

module.exports = html
