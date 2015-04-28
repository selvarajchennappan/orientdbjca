Orient DB JCA Resource Adapter for Weblogic 12c(any JEE7 Complaint Server)

Thanks to Mr Harald Wellmann. I had taken OPS4J Orient as a reference to develop.

I have developed this to enable global transaction(XA) between Orient DB and Relational DB.

1.	Connection Pool managed by Weblogic Container.
2.	To enable Orient DB resource manager to participate in XA transaction managed by JEE Container.
3.	Logging

Prerequisite:

Java 6+
Weblogic12c (12.1.2) 

Build and Deploy

I have used gradle to build and test. Hence click gradlw.bat/gradlew.sh to download gradle to local.

Download : orient-jca-resource-adapter

To use existing rar, get it from build/libs.

Type Gradlew rar 

it will generate rar in build/libs dir.

Deploy  the created/ existing  rar file as application in Weblogic 12c.

Monitor the connection pooling via weblogic admin console.
We can change the connection and logging property dynamically.

Created Application - Orient-Transaction  for testing 

Download : Orient-XATransaction 
	Rest Service exposed
                Used EJB to enable transaction
1.	saveIntoGraphDB
2.	saveIntoRelationalDB
3.	readFromGraphDB

To use existing war, get it from build/libs.

Type Gradlew war 

it will generate in build/libs.

Deploy the created war in Weblogic 12c


Testing:

Prerequisite:

1.Go to weblogic admin ,create datasource “RelationalDataSource” and ensure you have chosen XA connection .
2.Create database at OrientDB server

Steps:
Open IntegrationTest.groovy

Provide RDBMS and Graph DB URL

type Gradlew integrationTest 

1.	It creates table in RDBMS and class in graph DB
2.	It executes below test cases
1.	should insert into graph db and relational db successfully.
2.	insert into graph db successfully and unique key constraint during second time execution at relational db with same :#id
3.	graph db and relational db successfully inserted . However graph read failed,So rollback both
3.	drops table and class.
--------------------------------------------------------------------------------
To make both as eclipse project .
gradlew eclipse  and then  import as project into your workspace.


Please feel free to add /edit .







