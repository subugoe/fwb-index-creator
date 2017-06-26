# Index generator for FWB (Frühneuhochdeutsches Wörterbuch)

## Description

This tool creates and uploads Solr index files.
It uses specifically formatted TEI files to generate XML files containing fields for a Solr index.
The Schema and other files that must be used to configure Solr can be found in the solr/ directory.
There is also a .jar file that contains some modifications for Solr. You can find its source code here: 
https://github.com/subugoe/fwb-solr-mods.

## Quick install using docker-compose

- Clone the project and go into its main directory.

- Compile all modules:

``` docker-compose up ```

- Prepare the backend module (Solr):

``` cd fwb-backend ```

``` chmod a+w solr/fwb solr/fwboffline solr/*/core.properties ```

- Change the outside port in the docker-compose.yml, in section 'ports', e. g. to 4321:

``` - 4321:8983 ```

- Start Solr:

``` docker-compose up -d ```

- Prepare the frontend module (importer web page):

``` cd ../fwb-webapi ```

``` cp docker.env.dist docker.env ```

- Enter the correct user data and URLs into docker.env:

``` GIT_USER=... ```

- Change the outside port in the docker-compose.yml, in section 'ports', e. g. to 4322:

``` - 4322:8080 ```

- Start the importer web page:

``` docker-compose up -d ```

## Compilation without Docker

You need Java JDK 7 or higher and Maven 3 or higher.
To compile the project, go into its main directory and execute 

``` mvn clean package ```

The Java executable .jar files will be placed into the target/ directories of the two frontend modules (fwb-cli and fwb-webapi).

## Executing the command-line tool

You can execute the tool by typing

``` java -jar indexer.jar <options> ```

To see what options are available, type

``` java -jar indexer.jar -help ```

The main options are

* -convert
  * The tool will convert TEI files to Solr XML index files.
  
* -compare
  * This will run a check of the generated HTML inside the Solr files by comparing it to the text in the TEI inputs.

* -upload
  * All the generated XMLs will be uploaded to a given Solr URL.
  
* -test
  * Some specific queries will be sent to Solr to ensure that the index was created correctly.
  
There are several secondary options that are required for the main options to execute properly.
Examples are: directories for input and output files, Solr URL, etc.
Inspect the -help output to choose the correct ones.

  
  
