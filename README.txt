Dan Higgins
higgins@nceas.ucsb.edu
May 15, 2000

TopFrame.java is the 'top-level' class of the application. i.e. to launch use the command

java -cp .;otc.jar;xerces.jar;xalan.jar edu.ucsb.nceas.dtclient.TopFrame

The three jar files otc.jar, xalan.jar, and xerces.jar are required for the application to run. otc.jar contains a few classes from Symantec Visual Cafe used in the code. (These classes will probably disappear in the future.) xerces and xalan jar files can be downloaded from http://xml.apache.org/