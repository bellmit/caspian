README.txt
==========

** Folder Hireachy

imagerepository                     # parent project folder
    | -- imagerepository-api        # contains model defnitions and REST APIs defnitions
    | -- imagerepository-client     # Jersey client implementation
    | -- imagerepository-server     # Implementation of the service.

    
** Gradle commands 
* 'gradlew jar' builds the jar artificats.

* 'gradlew installApp' builds the server application with dependencies and startup scripts.
    Location of the built artefact is
    
    imagerepository-server/build/install/imagerepository-server
                                                | -- bin                                # this contains the startup script for running the imagerepository server
                                                |     | -- imagerepository-server.bat
                                                |     | -- imagerepository-server.sh
                                                | -- lib                                # this contains the jars required to run the server.
                                                      | -- *.jar
                                                      | -- imagerepository-api-1.0.jar
                                                      | -- imagerepository-server-1.0.jar
 
 
 * gradlew generateJsonSchema2Pojo
    This command is used to generate the POJOs from the Openstack JSON schema documents. The generated pojo files will be added to the class path.
    
