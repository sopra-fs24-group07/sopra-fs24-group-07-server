# SoPra RESTful Service Template FS24

## Getting started with Spring Boot

- Documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/index.html
- Guides: http://spring.io/guides
  - Building a RESTful Web Service: http://spring.io/guides/gs/rest-service/
  - Building REST services with Spring: https://spring.io/guides/tutorials/rest/

## Setup this Template with your IDE of choice

Download your IDE of choice (e.g., [IntelliJ](https://www.jetbrains.com/idea/download/), [Visual Studio Code](https://code.visualstudio.com/), or [Eclipse](http://www.eclipse.org/downloads/)). Make sure Java 17 is installed on your system (for Windows, please make sure your `JAVA_HOME` environment variable is set to the correct version of Java).

### IntelliJ

If you consider to use IntelliJ as your IDE of choice, you can make use of your free educational license [here](https://www.jetbrains.com/community/education/#students).

1. File -> Open... -> SoPra server template
1. Accept to import the project as a `gradle project`
1. To build right click the `build.gradle` file and choose `Run Build`

### VS Code

The following extensions can help you get started more easily:

- `vmware.vscode-spring-boot`
- `vscjava.vscode-spring-initializr`
- `vscjava.vscode-spring-boot-dashboard`
- `vscjava.vscode-java-pack`

**Note:** You'll need to build the project first with Gradle, just click on the `build` command in the _Gradle Tasks_ extension. Then check the _Spring Boot Dashboard_ extension if it already shows `soprafs24` and hit the play button to start the server. If it doesn't show up, restart VS Code and check again.

## Building with Gradle

You can use the local Gradle Wrapper to build the application.

- macOS: `./gradlew`
- Linux: `./gradlew`
- Windows: `./gradlew.bat`

More Information about [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) and [Gradle](https://gradle.org/docs/).

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

You can verify that the server is running by visiting `localhost:8080` in your browser.

### Test

```bash
./gradlew test
```

### Development Mode

You can start the backend in development mode, this will automatically trigger a new build and reload the application
once the content of a file has been changed.

Start two terminal windows and run:

`./gradlew build --continuous`

and in the other one:

`./gradlew bootRun`

If you want to avoid running all tests with every change, use the following command instead:

`./gradlew build --continuous -xtest`

## API Endpoint Testing with Postman

We recommend using [Postman](https://www.getpostman.com) to test your API Endpoints.

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

# Protected Branches

`main` branch is protected. Devs need to work on other branch and issue merge
requests. The following output will be returned when attempting to push to
`main`:

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
