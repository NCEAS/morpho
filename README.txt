7/28/00 - The contents of dmanclient have been completely reorganized
All java source code is now inside the src directory within subdirectories
representing the package structure where compiled classes should be stored.
Xalan.jar, xerces.jar and several other jar files needed for compilation
and execution are included in the lib directory.

You should be able to execute the Desktop Client immediately after 'checking
out' the code from CVS by executing runme.bat (Windows machines - Unix machines
should be able to run the program by typing the contents of runme.bat on
the command line.) Certain features (e.g. the metadata editor) will not operate
properly until paths in 'mde.cfg' are set properly for the user's machine,
however.

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
create a jar file, dmanclient.jar, which should contain everything needed to run the
application.

                          

Dan Higgins  (higgins@nceas.ucsb.edu)