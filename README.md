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
separate breakout rooms (separate voice channels) for each task wehere the
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
- [Puhser](https://pusher.com/)
- [Mailjet](https://www.mailjet.com/)
- [OpenAI ChatGPT
  API](https://openai.com/index/introducing-chatgpt-and-whisper-apis/)

## Launch and Development

Note: More details can be found in the documentation on confluence (Wiki ->
Backend).

1. Clone the project
1. Setup with the IDE of choice (see below for InelliJ or VS Code)
1. Install pre-commit hoocks:
   - Install pre-commit [here](https://pre-commit.com/)
   - Install pre-commit hoocks: `pre-commit install`
1. Fill in the *Environment Variables* in the [setup.sh](setup.sh) script
   - Obtain the credentials in the documentation on confluence (Wiki ->
     Backend).
1. Create a posgtres docker container
   - `docker create --name productiviteam -e POSTGRES_PASSWORD=$DB_PSW -p 5432:5432 postgres:11.5-alpine`
   - `docker start productiviteam`
1. Source the [setup.sh](setup.sh) script (no windows support, need to be set
   manually; works with git-bash)
   - `. setup.sh`
   - Choose the profile (description before prompt)
     environment variables needed
1. Build with gradle

- macOS: `./gradlew`
- Linux: `./gradlew`
- Windows: `./gradlew.bat`
- More Information about [Gradle
  Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) and
  [Gradle](https://gradle.org/docs/).

Alternative setup:

- Edit the "run configuration" in IntelliJ or VS Code by adding there the
  environment variables
- In this case you cannot build it via the terminal

### IntelliJ

If you consider to use IntelliJ as your IDE of choice, you can make use of your
free educational license
[here](https://www.jetbrains.com/community/education/#students).

1. File -> Open... -> SoPra server template
1. Accept to import the project as a `gradle project`
1. To build right click the `build.gradle` file and choose `Run Build`

### VS Code

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

If you want to avoid running all tests with every change, use the following command instead:

`./gradlew build --continuous -xtest`

## High-Level Components

## Roadmap

**Session Statistics**

- More detailed session statistics
- Track how many tasks were completed during a session

**Team Member Roles**

- A team admin role which has more permissions than the other team members
  (e.g. change team name/description, kick other team members out of the team)

**Security**

- Add secure login
- Add spring boot security

## Debugging

If something is not working and/or you don't know what is going on. We recommend using a debugger and step-through the process step-by-step.

To configure a debugger for SpringBoot's Tomcat servlet (i.e. the process you start with `./gradlew bootRun` command), do the following:

1. Open Tab: **Run**/Edit Configurations
1. Add a new Remote Configuration and name it properly
1. Start the Server in Debug mode: `./gradlew bootRun --debug-jvm`
1. Press `Shift + F9` or the use **Run**/Debug "Name of your task"
1. Set breakpoints in the application where you need it
1. Step through the process one step at a time

## Testing

Have a look here: https://www.baeldung.com/spring-boot-testing

```plaintext
$ git push
Enumerating objects: 5, done.
Counting objects: 100% (5/5), done.
Delta compression using up to 16 threads
Compressing objects: 100% (3/3), done.
Writing objects: 100% (3/3), 283 bytes | 283.00 KiB/s, done.
Total 3 (delta 2), reused 0 (delta 0), pack-reused 0
remote: Resolving deltas: 100% (2/2), completed with 2 local objects.
remote: error: GH006: Protected branch update failed for refs/heads/main.
remote: error: Changes must be made through a pull request. Cannot change this locked branch
To github.com:sopra-fs24-group07/sopra-fs24-group-07-server
 ! [remote rejected] main -> main (protected branch hook declined)
error: failed to push some refs to 'github.com:sopra-fs24-group07/sopra-fs24-group-07-server
```
