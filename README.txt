5/31/00 - The contents of dmanclient have been completely rearranged. 
All java source code is now inside the src directory within subdirectories
representing the package structure where compiled classes should be stored.
Xalan.jar and xerces.jar files have also been included since they are needed
for compilation.

One can compile the code by moving all *.java files to a single directory
and then using 'javac' to compile with the '-d' switch so that packages are
placed in the proper directories.

A much easier method is to use the java-based 'ant' program from www.apache.org.
Ant is a cross-platform utility designed to replace the Unix 'make' utility.
Ant can be obtained from "http://jakarta.apache.org/ant/index.html". 

The binary distribution of Ant consists of three directories: bin, docs and lib. 
Only the bin and lib directory are crucial for running Ant. To run
Ant, the following must be done:

     Add the bin directory to your path. 
     Set the ANT_HOME environment variable. This should be set to the directory 
         which contains the bin and lib directory. 
     Set the JAVA_HOME environment variable. 
         This should be set to the directory where the JDK is installed.   

Ant works by reading instructions from an XML file. In this case, the file is called
'build.xml'. One can open and read this file to see the steps needed to install the
desktop client code in this version of the Client. Simply CDing the base directory
and entering 'Ant' will run the build.xml file. This will compile the java code,
move the image files into the same directory as compiled class files, and then
create a jar file, XMLClient.jar, which should contain everything needed to run the
application.

NOTE: The current Ant binary distribution DOES NOT work properly with the newly
released Java 1.3 compiler. A fixed Javac.java source file is available, but one
currently needs to get that new source file and add it to the proper jar file
(ant.jar) replacing the older file.                          

Dan Higgins  (higgins@nceas.ucsb.edu)