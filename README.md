# Index generator for FWB (Frühneuhochdeutsches Wörterbuch)

## Description

This tool creates and uploads Solr index files.
It uses specifically formatted TEI files to generate XML files containing fields for a Solr index.
The Schema and other files that must be used to configure Solr can be found in the solr/ directory.
There is also a .jar file that contains some modifications for Solr. You can find its source code here: 
https://github.com/subugoe/fwb-solr-mods.

## Compilation

You need Java JDK 7 or higher and Maven 3 or higher.
To compile the project, go into its main directory and execute 

``` mvn clean package ```

The Java executable .jar file will be placed into the target/ directory.

## Executing the tool

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
  
  
