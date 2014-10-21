Regular Expression Experiment Package
=====================================

Prerequisites
-------------

Required Software:

* JDK >=6
* Building: Apache Maven 3

Building and execution
----------------------

For building and execution, here are some useful Maven commands to get started:

	# Build a JAR
	mvn package -DskipTests -Dmaven.test.skip=true
	# Execute
	mvn exec:java
	# Build a JAR with all dependencies
	mvn assembly:single
	# Execute JAR with all dependencies
	java -jar target/*jar-with-dependencies.jar
	# Execute JAR, shell mode with full debug output, output log to file
	mvn exec:java -Dexec.args="-d shell" -e | tee target/log.txt

Hints
-----

User configuration: `src/main/resources/local.properties`

* `dir.base`: Base directory for outputting all schema analysis data
* `env.log.color`: true/false for using color in cli output
* `cse.cx`: Google Custom Search CX
* `cse.apikey`: Google API Key

Google Search:

* Google search is performed with the CSE API.
* The *API Key* and *CX* value **NEED** to be set in local.properties.
* For this, a *CSE* must be set up: [https://code.google.com/apis/console](https://code.google.com/apis/console)
* A Google account is required, 100 queries per day are free.
* More info: [http://code.google.com/intl/de/apis/customsearch/v1/overview.html](http://code.google.com/intl/de/apis/customsearch/v1/overview.html)
