
### For presenters
[Presentation link](https://docs.google.com/presentation/d/1cdwL4OQg93SLHktC_x0qswUdh2A_mOwrBsBTzv0nqcE/edit?slide=id.g365979ec388_0_952#slide=id.g365979ec388_0_952)

# Restructuring codebase
The very simple helloworld application that has been used has been 
designed to be simple and highlight some of the Temporal specific 
primatives.  (Workflow, activity, worker) A real project will generally 
be significantly more complex and by using a standard scaffolding approach 
will make either re-factoring to make code re-usable by other teams or to 
improve modularity for maintanence.

Having a structure that is similar to the below example can help this modularisation:

`/api` - A location for the definition of API endpoints.  (Particularly REST endpoints that clients can call to start, signal, query or update a workflow.)

`/domain` - Business rules should be encapsulated in a domain.  This is where the heart of the app will reside.

`/domain/messages`
Temporal is a messaging based system so this package captures the 
contracts used to interact with the domain.  Essentially the objects 
used to communicate with a workflow.  Be that for starting, signaling, 
updating or querying the workflow and for interacting with activities.

Any parameters that are passed in to either a workflow or activity should 
be defined as a class in their own right.  This makes maintenance a little 
easier as changes to the object to add/remove attributes do not result in
non-deterministic errors in the workflow.  (Checks are made on the activity 
signatures but not on the content)  

/`domain/workflows`
This holds the orchestration implementations and adapter 
implementations. (activities) 


# Exercise
# Step 1
You will use the code from exercise 1 to refactor the application.
Under io/temporal create a package "app"  (App for this exercise but you would use your app name)
Under this package:

```
app
--> api
   --> controllers
--> domain
   --> integrations
   --> workflows
      --> hello 
   --> messages
--> workers
```

Currently nothing needed in the controllers package later in this exercise we will create a controller for managed testing.

Move the activities from the hello package into integrations.

Refactor the workflow from /io/temporal/workflow to /io/temporal/app/workflows


Run the app to ensure that all the refactoring has worked.  (I used the IDE to refactor which normally does a good job of moving packages about but you may have to look at the imports to correct.)

```bash 
./mvnw spring-boot:run -Dspring-boot.run.profiles=hello
```

# Step 2
Create the class to hold the "name" used for saying hello and use in workflows 
and activities.  This is good practice as it means the class can have changes made 
to it (adding or removing attributes) without impacting the signature of the WF or activity
methods.  This minimises the risk of non-deterministic errors.

```aiignore
package io.temporal.app.domain.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Name {

    String firstName = new String();
    String lastName = new String();
    String preferedLanguage = "en";

    public Name() {}

    public Name(String fullName) {
        var parts = (fullName == null ? "" : fullName.trim()).split("\\s+", 2);
        this.firstName = parts[0].isEmpty() ? null : parts[0];
        this.lastName = parts.length == 2 ? parts[1] : null;
    }

    @JsonIgnore
    public String getName() {
        return firstName + " " + lastName;
    }
}

```
Note - I used the lombok library to generate the getters and setters.  If you follow this then it is necessary to add the dependency to the pom.xml

```aiignore
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
```

Change the workflow and activity interfaces and implementations to use the new Name class rather than a String.  We used the StarterRunner to start a workflow automatically if the profile used was "hello" this means the StarterRunner class also needs to be adjusted to use the new Name class.  
(You also need to change the test class to use the new Name class ensure the assertion is correct)

Re-run the application and tests.

# Step 3
Add a REST endpoint so that the workflow can easily be started.  To do this create the REST controller in the api/controllers directory.

```aiignore
package io.temporal.app.api.controllers;

import java.util.UUID;

import io.temporal.app.domain.messages.Name;
import io.temporal.app.domain.workflows.hello.HelloWorldWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloRESTController {

    private final WorkflowClient workflowClient;
    private final String taskQueue;

    HelloRESTController(WorkflowClient workflowClient,
                        @Value("${spring.temporal.workers[0].task-queue}") String taskQueue) {
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
    }

    @PostMapping("/hello")
    String hello(@RequestBody Name name) {
        var options = WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId("hello-" + UUID.randomUUID())
                .build();

        var workflow = workflowClient.newWorkflowStub(HelloWorldWorkflow.class, options);
        return workflow.sayHello(name);
    }
}
```
In the example controller it uses a workflow client to start the workflow directly in the controller.  In a more complex environment all interactions with temporal may be taken out into a manager/utility class.  Once the controller is in place then you can use curl to simply create a workflow.

```bash
curl -X POST http://localhost:3030/hello \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Xiao","lastName":"Zhan"}'
```
Assuming all worked then we want to be able to start the application with the default 
profile to be able to start workflows via curl.  This means making some changes so that the 
default profile starts up the worker with the workflows and activities automatically 
registered.  This means making some changes to add key data to the application.yml file.

```aiignore
...
    workers-auto-discovery:
      packages:
        - io.temporal
    workers:
      - task-queue: HelloSampleTaskQueue
...
```
Having made this change we can start the app without a profile and cause a workflow to start via curl.
```bash
./mvnw test

./mvnw spring-boot:run -f pom.xml
```
```bash
curl -X POST http://localhost:3030/hello \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Xiao","lastName":"Zhan"}'
Hello, Xiao Zhan!
```
# Step 4
It is common to want to perform custom configuration of the worker 
and temporal provides a mechanism to do this.  With spring boot this can be provided
via the configuration annotation that applies the cusomizations to the 
auto-created worker.

To add the options in create a class call TemporalOptionsConfig in a app/config package.  
The [sample springboot application](https://github.com/temporalio/samples-java/blob/main/springboot/src/main/java/io/temporal/samples/springboot/customize/TemporalOptionsConfig.java) has an example of the content for this
file.  Spring boot will automatically apply the configuration to the worker.

As a test to show that this configuration file is being picked up we will simply change the
"identity" of the worker.  Further configuration may be done in later exercises to apply
specific configuration.

In the `WorkerOptionsCustomizer` add a line to set the identity.

```aiignore
...
    @Bean
    public WorkerOptionsCustomizer customWorkerOptions() {
...
                // Customize the name of the worker.
                optionsBuilder.setIdentity("HelloWorldAppInstance");
...
```
Then re-run the application.  Issue the curl command to create a workflow instance then in 
the Temporal UI inspect the workflow which will allow you to click on the "workers"
tab and see that the worker with your custom identity has been started.


