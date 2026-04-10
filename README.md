# Temporal Booking Workshop

A Spring Boot application for running Temporal workers.

## Prerequisites

- Java 21
- Temporal server running locally on `127.0.0.1:7233`

Start a local Temporal server with the [Temporal CLI](https://docs.temporal.io/cli):

```bash
temporal server start-dev
```
Each exercise will either have a source available or will be using the source/solution from the previous exercise. In all cases the assumption is that you are using a local temporal server instance unless otherwise specified.  The instructions above detail the requirements you must have on your laptop to run the exercises.

## Workshop Agenda
### [Exercise 1](run-a-simple-workflow/exercise/README.md) - Run a simple workflow.
* Running a simple application to start a workflow, execute a task and complete.
* Understand the "best practices" layout/organisation of code

### [Exercise 2](introduce-interceptors/exercise/README.md) - Introduce Interceptors
* Adding a custom metric to count the number of retries of an activity
* View metrics emitted from application

### [Exercise 3](use-interceptor-to-handle-auth-failure/exercise/README.md) - use interceptor to handle auth failure

### [Exercise 4](applying-best-practices/exercise/README.md) - Applying best practices

### [Exercise 5](understand-temporal-integration-with-spring-boot/exercise/README.md) - Understand Temporal integration with Spring Boot


### [Exercise 6](testing/exercise/README.md) - Testing
* Basics of unit testing
* Using a replay test

### [Exercise 7](worker-versioning/exercise/README.md) - Worker Versioning
* Add a version to your worker and view unpinned workflow migrating to current release.  
* Show pinned workflow remaining on old worker.
* Adding version testing

### [Exercise 8](priority-and-fairness/exercise/README.md) - Priority and Fairness
* Include priority into your app processing
* Demonstrate fair share processing

### [Exercise 9](saga-pattern-implementation/exercise/README.md) - Saga Pattern implementation
* Understand scopes
* Demonstrate applying compensation steps.

### [Exercise 10](understanding-metrics/exercise/README.md) - Understanding metrics

### [Exercise 11](dynamic-workflows-and-dsl/exercise/README.md) - Dynamic Workflows & DSL

