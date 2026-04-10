
### For presenters
[Presentation link](https://docs.google.com/presentation/d/1cdwL4OQg93SLHktC_x0qswUdh2A_mOwrBsBTzv0nqcE/edit?slide=id.g365979ec388_0_952#slide=id.g365979ec388_0_952)

# Restructuring codebase
The very simple helloworld application that has been used has been 
designed to be simple and highlight some of the Temporal specific 
primatives.  (Workflow, activity, worker) A real project will generally 
be significantly more complex and by using a standard scaffolding approach 
will make either re-factoring to make code re-usable by other teams or to 
improve modularity for maintanence.

Having a structure:

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
app
--> api

   --> controllers

--> domain

   --> workflows

      --> activities

      --> hello 

      --> messages

--> workers


Currently nothing needed in the controllers package but if we were to expose REST endpoints they would logically be here.
Move the activities from the hello package into activites
Move the Application file under workers.

Run the app to ensure that all the refactoring has worked.  (I used the IDE to refactor which normally does a good job of moving packages about but you may have to look at the imports to correct.)

```bash 
./mvnw spring-boot:run -Dspring-boot.run.profiles=hello
```

Step 2 - Create the class to hold the "name" used for saying hello and use in workflow/activities

TODO 