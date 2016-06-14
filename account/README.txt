README.txt
==========

** Folder Hireachy

account                 # Parent project folder
    | -- api            # REST APIs interface definition
    | -- client         # REST client implementation
    | -- datacontract   # Definition of objects passed to and from REST APIs
    | -- model          # Definition of underlying data model and mysql accessors
    | -- server         # Implementation of the service.
    | -- util           # Utility functions and classes used by the project
    
** Gradle commands 
* 'gradle jar' builds the jar artificats.

* 'gradle installApp' builds the server application with dependencies and startup scripts.
    Location of the built artefact is
    
    server/build/install/imagerepository-server
        | -- bin                                # this contains the startup script for running the account service
        |     | -- server.bat                   # startup script for windows 
        |     | -- server.sh                    # startup script for unix
        | -- lib                                # this contains the jars required to run the server.
              | -- *.jar
              | -- api-1.0.jar
              | -- datacontract-1.0.jar
              | -- model-1.0.jar
              | -- server-1.0.jar
              | -- util-1.0.jar
