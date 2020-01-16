# ![](frontend/app/src/main/res/mipmap-hdpi/ic_launcher2.png) Task Manager Pro

**Task Manager Pro** is an Android application that manages projects for several users. Organize your projects between your favorites or the ones that have the nearest deadline, download a report summarizing the project, attach pictures or documents and share them between the members of the group or even use ML to create tasks using only a photo. Task Manager PRO is the true new meaning to the concept of a task manager app.

## STRUCTURE OF THE REPOSITORY

The repository is mainly based on two very different and separate parts:

```
.
├── Dockerfile
├── README.md
├── deploy.sh
├── frontend
└── server

```

- **frontend** contains all the Android app code
- **server** contains all the cloud functions in firebase as well as the api server running in Google App Engine
- **deploy.sh**  builds and run the Andriod and application and the cloud service modules.

## BUILD AND RUN SCRIPT

To build and run the application you can use the `deploy.sh` in the project root folder:

```bash
./deploy.sh
```

The script builds a Docker image that will be able to build the Android app and installs it in a connected device.


## API ENDPOINTS

Base URL: <https://mcc-fall-2019-g10.appspot.com>

### `/user`

#### /

- POST

  create a user

  ```bash
  curl --request POST \
    --url https://mcc-fall-2019-g10.appspot.com/user \
    --header 'content-type: application/json' \
    --data '  {
      "id": "1111",
      "name": "test11",
      "email": "test@test.test"
    }'
  ```

#### /p/:userId

- GET

  This one populates the projects field with data

  ```bash

  curl --request GET \
    --url https://mcc-fall-2019-g10.appspot.com/user/p/2

  ```

#### /:userId

- GET

  get user information

  ```bash
  curl --request GET \
    --url https://mcc-fall-2019-g10.appspot.com/user/1111
  ```

- DELETE
- PUT

  similar to GET

#### /:userId/meta/keywords

- POST

  get all keywords from the project where the user is a member

  ```bash
  curl --request POST \
  --url https://mcc-fall-2019-g10.appspot.com/user/2/meta/keywords \
  --header 'content-type: application/json'
  ```


#### /:userId/search

- POST

  ```bash

    # search by 1 key word
    curl --request POST \
      --url https://mcc-fall-2019-g10.appspot.com/user/2/search \
      --header 'content-type: application/json' \
      --data '{
      "on": "projects",
      "byKeyword": "it"
    }'

    curl --request POST \
      --url https://mcc-fall-2019-g10.appspot.com/user/2/search \
      --header 'content-type: application/json' \
      --data '{
      "on": "projects",
      "byName": "thing",
      "fromStringStart": false
    }'

  ```

#### /:userId/favorites/:projectId

- PUT/DELETE

  Add a project as user's favorite

  ```bash
  # the example is delete
  curl --request DELETE \
    --url https://mcc-fall-2019-g10.appspot.com/user/2/favorites/xaYQ46XygjEakKjF3Qgaq3
  ```

#### /search?pre=test

- GET

  search user by username

  ```bash
  curl --request GET \
    --url 'https://mcc-fall-2019-g10.appspot.com/user/search?pre=test'
  ```


### `/project`

  #### /

  - POST

    create a new project

    ```bash
    curl --request POST \
      --url https://mcc-fall-2019-g10.appspot.com/project \
      --header 'content-type: application/json' \
      --data '{
      "admin": "3",
      "name": "something",
      "description": "blablabla",
      "type": "Personal",
      "deadline": "2019-11-10T13:18:15.696Z",
      "keywords": ["try", "it", "a bit"]
    }'

    # admin must exist in the user database for this to work!
    # deadline must be formatted as ISOString
    ```

  #### /:projectId

  - GET
  - DELETE
  - PUT

    ```bash

      curl --request PUT \
        --url https://mcc-fall-2019-g10.appspot.com/project/aWJdMuyyMk7iuEpZwJuxLq \
        --header 'content-type: application/json' \
        --data '{
        "description": "ohlala"
      }'

      curl --request GET \
        --url https://mcc-fall-2019-g10.appspot.com/project/aWJdMuyyMk7iuEpZwJuxLq

    ```


  #### /:projectId/members/:memberId

  - PUT

    add a single user to the project

  - DELETE

    delete a single user to the project

  #### /:projectId/members

  - PUT

    add a list of users to the project

  - DELETE

    delete a list of users from the project

    ```bash
    curl --request PUT \
      --url https://mcc-fall-2019-g10.appspot.com/project/aWJdMuyyMk7iuEpZwJuxLq/members \
      --header 'content-type: application/json' \
      --data '["2", "1111"]'

    # userId should exist in user database
    ```


  #### /:projectId/tasks`

  - POST

    add tasks(can be multiple) to a project

    ```bash
    # optional deadline field for a task, format ISOString
    curl --request POST \
      --url https://mcc-fall-2019-g10.appspot.com/project/aWJdMuyyMk7iuEpZwJuxLq/tasks \
      --header 'content-type: application/json' \
      --data '[
        {
        "description": "test",
        "status": "completed",
        "name": "haha"
        },
        {
        "description": "test",
        "status": "completed",
        "name": "heihei"
        }
      ]'
    ```


  #### /:projectId/tasks/:taskId/assigned_to/:userId

  - PUT

    ```bash
        curl --request POST \
        --url https://mcc-fall-2019-g10.appspot.com/project/aWJdMuyyMk7iuEpZwJuxLq/tasks \
        --header 'content-type: application/json' \
        --data '[
          {
          "description": "test",
          "status": "completed",
          "name": "haha"
          "assigned_to: ["21"]
          },
          {
          "description": "test",
          "status": "completed",
          "name": "heihei",
          "assigned_to: ["21"]
          }
        ]'
    ```

  - DELETE

    delete multiple tasks

    ```bash
    curl --request DELETE \
      --url https://mcc-fall-2019-g10.appspot.com/project/aWJdMuyyMk7iuEpZwJuxLq/tasks \
      --header 'content-type: application/json' \
      --data '[
        "cFMB2WnZHhdSdHkViCK1be",
        "wTjFF2MgBudh5iDQEBTHvi"
    ]'
      # !! replace the data with prop ids found in the firestore before trying it
    ```

  #### /:projectId/tasks/:taskId`

  - PUT

    update a single task

  - DELETE

    delete a single task

    ```bash
    curl --request PUT \
      --url https://mcc-fall-2019-g10.appspot.com/project/aWJdMuyyMk7iuEpZwJuxLq/tasks/3HPonNrT2iTQVEvmMZnAb2 \
      --header 'content-type: application/json' \
      --data '    {
        "description": "test!!!",
        "status": "ongoing",
        "name": "ngas"
        }'
    ```


  #### /:projectId/tasks/:taskId/assigned_to/:userId

  - PUT

    assign a task to a user

  - DELETE

    remove assignment

    ```bash
    curl --request DELETE \
      --url https://mcc-fall-2019-g10.appspot.com/project/xaYQ46XygjEakKjF3Qgaq3/tasks/iA1p2RYSC6ZYN3s6wXjMQn/assigned_to/2111
    ```



  #### /:projectId/attachments

  - POST
  - DELETE

  similar to the tasks api
  
  

### `/pdf` (as cloud function)

  #### /projectId

  - GET

      ```bash
    
        curl --request GET \
        --url https://us-central1-mcc-fall-2019-g10.cloudfunctions.net/pdf/$projectId
    
      ```



## 👤 **Collaborators**
- [@adriacabeza](https://github.com/adriacabeza)
- [@Zhongyuan Jin](https://github.com/jzyxyz)
- [@xmartin46](https://github.com/xmartin46)
- [@rlongares](https://github.com/Rlongares)
