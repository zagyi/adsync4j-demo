adsync4j-demo
=============

Demo project that uses the [ADSync4J](https://github.com/zagyi/adsync4j) library.

#### What does it do?
The application retrieves all normal user objects from Active Directory, and keeps them in sync by performing incremental synchronization with AD on subsequent executions. The replicated objects are stored in a H2 database in `$USER_HOME/adsync4j.h2.db`.

#### How to start playing with it?

###### Clone and build the project
	git clone https://github.com/zagyi/adsync4j-demo.git
	cd adsync4j-demo
	./gradlew build

###### Create a .properties file defining the necessary details
You need to give some details about how to connect to Active Directory, and what exactly you want to replicate. [This template](src/main/resources/dcaffiliation_template.properties) will help you to quickly create such a file.

###### Run the initial full synchronization
The build process created a shell script that you can use to start the application:

	./adsync4j-demo.sh <path_to_your_properties_file>

The H2 database is automatically created on the first synchronization. Once that is successfully done, you can throw away the .properties file, because all the information it holds gets persisted in the same database.

###### Keep the replica up-to-date with incremental synchronization
Simply execute the application again and again (without specifying the .properties file) to apply any changes to the replica that has been done in Active Directory since the last synchronization.
