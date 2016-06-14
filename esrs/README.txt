README.txt
==========

** Folder Hireachy

esrs                     # parent project folder
    | -- esrs-api        # contains REST APIs defnitions
    | -- esrs-model      # contains model defnitions
    | -- esrs-server     # Implementation of the service.

    
** Gradle commands 
* 'gradlew build' builds the jar artificats.

* 'gradlew installApp' builds the server application with dependencies and startup scripts.
    Location of the built artefact is
    
    /esrs/server/build/install/server
                                                | -- bin                                # this contains the startup script for running the esrs server
                                                |     | -- server.bat
                                                |     | -- server
                                                | -- lib                                # this contains the jars required to run the server.
                                                      | -- *.jar
                                                      | -- api-1.0.jar
                                                      | -- server-1.0.jar
													  | -- model-1.0.jar