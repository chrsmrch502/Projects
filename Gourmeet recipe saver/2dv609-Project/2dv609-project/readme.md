## Prerequisites
* Install MySQL Community Server (Download and Instructions: https://dev.mysql.com/downloads/mysql/)
* Install Java Development Kit (JDK) (Download and Instructions: https://docs.oracle.com/en/java/javase/16/install/overview-jdk-installation.html)
* Knowledge of
	* Your database host
	* Your database port
	* Your database user
	* Your database password
* Create and set up a database (choose A or B)
	* A - Via MySQL Workbench:
		* Connect to an instance
		* Go to tab "File"
		* Select "Open SQL script.."
		* Locate and open the file "script-01-create_and_setup_db_and_tables.sql"
		* Go to tab "Query"
		* Select "Execute (All or Selection)"
		* In the Navigator panel to the left, click the refresh icon next to the text "SCHEMAS"
		* The database "lnu_gourmeet" should now be visible
		* Repeat for the file "script-02-insert_data.sql"
	* B - Via MySQL Shell:
		* Locate the file "script-01-create_and_setup_db_and_tables.sql" and open in a text editor
		* Log in to an instance via MySQL Shell
		* Copy and paste the content of the file into the shell and execute it
		* Repeat for the file "script-02-insert_data.sql"
* To be able to compile the project yourself, maven needs to be installed (Download and Instructions: https://maven.apache.org/download.cgi)

## Running Gourmeet (Option 1)
The simplest way of running Gourmeet is to simple run the Gourmeet.jar file from the command line:

```
$ cd ../2dv609-project (navigate to root directory)
$ java -jar Gourmeet.jar [args]
```
[args] should be replaced with: 

```DB_HOST DB_PORT DB_USER DB_PASSWORD```
 
or 

```DB_PASSWORD``` (default values for DB_HOST: localhost, DB_PORT: 3306, DB_USER: root)

Example:
```
$ java -jar Gourmeet.jar localhost 3306 root p@ssw0rd
```

## Running Gourmeet (Option 2)
The second way of running Gourmeet is to compile the project yourself:

```
$ cd ../2dv609-project (navigate to root directory)
$ mvn compile
$ mvn exec:java -Dexec.mainClass=Gourmeet -Dexec.args="[args]"
```

[args] should be replaced with: 

```DB_HOST DB_PORT DB_USER DB_PASSWORD```
 
or 

```DB_PASSWORD``` (default values for DB_HOST: localhost, DB_PORT: 3306, DB_USER: root)

Example:
```
$ mvn exec:java -Dexec.mainClass=Gourmeet -Dexec.args="localhost 3306 root p@ssw0rd"
```