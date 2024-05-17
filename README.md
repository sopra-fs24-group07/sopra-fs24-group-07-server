# ProductiviTeam

ProductiviTeam is a online platform which helps students to organize
themselves. You can create teams and invite you peers. In sessions you can work
in a dedicated time frame on your previously defined tasks.

The goal of this application is to increase the productivity of small teams. As
students we want to help other students to coordinate themselves in teams when
working in group projects.

## Functionality

In order to help teams to coordinate, plan and execute a project, we have
implemented a task system that follows the Kanban methodology. Team members can
mark tasks which they want to do in the next group session and comment on
those.

When planning a session, the members can set a time goal on how long they want
to work in that session. During the session a voice chat is started with
separate breakout rooms (separate voice channels) for each task where the
designated team members of that task can work without disturbance. Teams can
track their previous session and see how long they have worked on each.

## Technologies

- Gradle
- Java Spring Boot
- REST Interface
- Google Cloud Project
  - App Engine
  - Cloud SQL (Postgresql, persistent)
- [Agora.io](https://www.agora.io/en/) Voicechat
- [Pusher](https://pusher.com/)
- [Mailjet](https://www.mailjet.com/)
- [OpenAI ChatGPT
  API](https://openai.com/index/introducing-chatgpt-and-whisper-apis/)

## Launch and Development

**Note**: More details can be found in the documentation on Confluence (Wiki ->
Backend). If you start contributing, you will get invited to the Confluence
project.

### 1. Clone Project

- `git clone <url>`

### 2. Install Pre-Commit Hooks

- Download [pre-commit](https://pre-commit.com/)
- Execute the following commands in the root of the project

```bash
pre-commit --version  # verify that pre-commit is installed correctly
pre-commit install    # install the hooks
```

Every time you make a commit, the pre-commit hooks will run through to ensure
the formatting (possibly among other future things) is correct. If not it will
reformat and you can add formatted files and commit again. This ensures a
uniform code formatting accross all contributors.

### 3. Setup [setup.sh](setup.sh) Script

**Linux/MacOS**

- We have a helper shell script with loads all the needed environment variables
  (credentials) which are needed
- Credentials can be found on Confluence
- Follow the following instructions, after setting the credentials in the
  [setup.sh](setup.sh) script under *Environment Variables*
  - This is interactive and you will be asked which profile to choose (more
    information in the prompt)

```bash
. setup.sh
```

**Windows**

- (*Not tested*) Take a look a the [setup.sh](setup.sh) script and manually set
  the environment variables in the system settings
  - [www.alphr.com/environment-variables-windows-10](https://www.alphr.com/environment-variables-windows-10/)
- Alternative setup:
  - Edit the "run configuration" in IntelliJ or VS Code by adding there the
    environment variables
  - In this case you cannot build it via the terminal
- Alternative setup:
  - Use the git bash and follow instructions for Linux/MacOS

### 4. Setup Local Postgresql Docker Container

- Install docker: [docs.docker.com](https://docs.docker.com/engine/install/)
- If you have followed the credentials on Confluence, you can execute the
  following commands

```bash
docker create --name productiviteam -e POSTGRES_PASSWORD=$DB_PSW -p 5432:5432 postgres:11.5-alpine
docker start productiviteam
```

### 5. Variant 1: Setup Project with IntelliJ

If you consider to use IntelliJ as your IDE of choice, you can make use of your
free educational license
[here](https://www.jetbrains.com/community/education/#students).

1. File -> Open... -> SoPra server template
1. Accept to import the project as a `gradle project`
1. To build right click the `build.gradle` file and choose `Run Build`

### 5. Variant 2: Setup Project with VS Code

The following extensions can help you get started more easily:

- `vmware.vscode-spring-boot`
- `vscjava.vscode-spring-initializr`
- `vscjava.vscode-spring-boot-dashboard`
- `vscjava.vscode-java-pack`

**Note:** You'll need to build the project first with Gradle, just click on the
`build` command in the _Gradle Tasks_ extension. Then check the _Spring Boot
Dashboard_ extension if it already shows `soprafs24` and hit the play button to
start the server. If it doesn't show up, restart VS Code and check again.

### Build

- Requirement: Environment variables set (via setup [setup.sh](setup.sh)
  script)

```bash
./gradlew build
```

### Run

- Requirement: Environment variables set (via setup [setup.sh](setup.sh)
  script)

```bash
./gradlew bootRun
```

You can verify that the server is running by visiting `localhost:8080` in your
browser.

### Test

- Requirement: Environment variables set (via setup [setup.sh](setup.sh)
  script)

```bash
./gradlew test
```

### Development Mode

- Requirement: Environment variables set (via setup [setup.sh](setup.sh)
  script)

You can start the backend in development mode, this will automatically trigger
a new build and reload the application once the content of a file has been
changed.

Start two terminal windows and run:

`./gradlew build --continuous`

and in the other one:

`./gradlew bootRun`

If you want to avoid running all tests with every change, use the following
command instead:

`./gradlew build --continuous -xtest`

### Contributing

**Branches**

- The `main` branch is protected and can only be modified via pull requst
- The `dev` branch will be used for development, and merged only into `main` if
  a milestone has been reached

**Pull Requets**

- The checkboxes serve as a reminder what should be done before a pull request

## High-Level Components

### Authorization Service

[AuthorizationService.java](src/main/java/ch/uzh/ifi/hase/soprafs24/service/AuthorizationService.java)

- Because we have teams, we do not want to allow all users to see or modify
  other teams which they are not part of
- This is why for every endpont we first check, if a users is allowed to do the
  requested action
- The follwing scopes are used
  - `isAuthorized`: If the passed `userToken` exists
    - E.g. Create a new team
  - `isExistingAndAuthorized`: Checks if the passed `userToken` exists and is
    the same as the token for the requested user
    - E.g. Modify the username of a user
  - `isAuthorizedAndBelongsToTeam`: Checks if the passed `userToken` exists and
    is the same as the token for the requested user\` and is part of the
    requested team
    - E.g. Create/Get/Modify/Delete tasks in a team

This is a core component, because for every request, the corresponding
permissions are checked this way.

### Team User Service

[TeamUserService.java](src/main/java/ch/uzh/ifi/hase/soprafs24/service/TeamUserService.java)

- Is responsible to manage the links between a users and a teams
- Creating a new link entry means a user joins the team
  - This will happen automatically when creating a team, meaning the user that
    creates the team will automatically join
  - When a new user joins with an invitation link, we only get the UUID of the
    team, thus first need to get the team with that UUID
- It can retrieve all users of a team
- It can retrieve all teams of a user
- It can delete a user from a team, which will be called if the users leaves
  the team

### Pusher Service

[PusherService.java](src/main/java/ch/uzh/ifi/hase/soprafs24/service/PusherService.java)

- This is responsible to ensure the real time interaction between users, such
  that every user can see what other users modified something
  - E.g. When one user creates a new task, the other users can see the new task
    popping up immediately
- For this we have multiple channels, where the clients subscribe to, and get
  notfied when an update occurs
  - If they get notified of an update, they will call this server to fetch the
    most recent data

Although this is a small service class, it is one of the most important one

## Roadmap

**Session Statistics**

- More detailed session statistics
- Track how many tasks were completed during a session
- Redo the database to save more information about each session

**Team Member Roles**

- A team admin role which has more permissions than the other team members
  (e.g. change team name/description, kick other team members out of the team)

**Security**

- Add secure login
- Add spring boot security

## Authors & Acknowledgement

- Derboven, Timon ([@Monti934](https://github.com/Monti934))
- Furrer, Basil ([@B1s9l](https://github.com/b1s9l))
- Greuter, Sven ([@5v3nn](https://github.com/5v3nn))
- Karatasli, Alihan ([@Alihan26](https://github.com/Alihan26))

Based on
[HASEL-UZH/sopra-fs24-template-server](https://github.com/HASEL-UZH/sopra-fs24-template-server)

## License

See [LICENSE](./LICENSE)
