# Temporal Booking Workshop

A Spring Boot application for running Temporal workers.  The purpose of this application is to ensure that the environment is correct and you are able to run the workflow and see the output.


## Running the Application
Simply running the application will start it up with no workers running.
```bash
./mvnw spring-boot:run
```
In order to ensure there are workers running and to execute a test workflow use the hello profile.
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=hello
```
Using this profile spring auto discovers the workflows and activities to register against the worker and uses the StarterRunner to automatically create a new workflow instance.

Ensure the app starts up cleanly and observe the workflow execution in the Temporal UI.



