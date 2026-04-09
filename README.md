# Temporal Booking Workshop

A Spring Boot application for running Temporal workers.

## Prerequisites

- Java 21
- Temporal server running locally on `127.0.0.1:7233`

Start a local Temporal server with the [Temporal CLI](https://docs.temporal.io/cli):

```bash
temporal server start-dev
```

## Workshop Agenda
### [Exercise 1](exercise01/README.md) - Get a simple "Hello World" Temporal Springboot application running and understand the project structure.
* Running a simple application to start a workflow, execute a task and complete.
* Understand the "best practices" layout/organisation of code

### [Exercise 2](exercise02/README.md) - Introduce Interceptors
* Adding a custom metric to count the number of retries of an activity
* View metrics emitted from application

### [Exercise 3](exercise03/README.md) - Showcase context propagation and using interceptors to detect auth failure and refresh token

### [Exercise 4](exercise04/README.md) - Applying best practices to repo layout

### [Exercise 5](exercise05/README.md) - Using the options of Spring Boot integration with Temporal.


### [Exercise 6](exercise06/README.md) - Testing
* Basics of unit testing
* Using a replay test

### [Exercise 7](exercise07/README.md) - Worker Versioning
* Add a version to your worker and view unpinned workflow migrating to current release.  
* Show pinned workflow remaining on old worker.
* Adding version testing

### [Exercise 8](exercise08/README.md) - Priority and Fairness
* Include priority into your app processing
* Demonstrate fair share processing

### [Exercise 9](exercise09/README.md) - Saga Pattern implementation
* Understand scopes
* Demonstrate applying compensation steps.

### [Exercise 10](exercise10/README.md) - Intro to the metrics and configuring your app appropriately.

### [Exercise 11](exercise11/README.md) - Dynamic Workflows & DSL

