This directory contains an example of using the XNI/PSVI code in Xerces 2.2.1 to get the structure of an XML Schema (specifically eml 2.0).

Just run the Windows batch file 'runnit.bat' to see the example.  

Click on 'Global Elements' and then on the top level eml node. If one then clicks the "Save" button, an output XML Schema template file will be written to the working directory with the name "SchemaOut.xml".

Note that the versions of Xerces jars included here support the getAnnotations method, so that help information can be pulled directly out of the Schema along with the PSVI object model.