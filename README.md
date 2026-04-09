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
### Exercise 1 - Get a simple "Hello World" Temporal Springboot application running and understand the project structure.
* Running a simple application to start a workflow, execute a task and complete.
* Understand the "best practices" layout/organisation of code

### Exercise 2 - Introduce Interceptors
* Adding a custom metric to count the number of retries of an activity
* View metrics emitted from application

### Exercise 3 - Showcase context propagation and using interceptors to detect auth failure and refresh token

### Exercise 4 - Using the options of Spring Boot integration with Temporal.


### Exercise 5 - Testing
* Basics of unit testing
* Using a replay test

### Exercise 6 - Worker Versioning
* Add a version to your worker and view unpinned workflow migrating to current release.  
* Show pinned workflow remaining on old worker.
* Adding version testing

### Exercise 7 - Priority and Fairness
* Include priority into your app processing
* Demonstrate fair share processing

### Exercise 8 - Saga Pattern implementation
* Understand scopes
* Demonstrate applying compensation steps.

### Exercise 9 - Intro to the metrics and configuring your app appropriately.

### Exercise 10 - Dynamic Workflows & DSL

