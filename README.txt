Morpho: XML Metadata Client  
---------------------------

Version: 1.1.1 - May 9, 2002

Feedback and bugs to: knb-software@nceas.ucsb.edu
                      http://bugzilla.ecoinformatics.org

Contributors: 
    Matt Jones (jones@nceas.ucsb.edu)
    Dan Higgins (higgins@nceas.ucsb.edu)
    Chad Berkley (berkley@nceas.ucsb.edu)
    Jivka Bojilova (bojilova@nceas.ucsb.edu)
    Chris Jones (cjones@lifesci.ucsb.edu)
    Mark Schildhauer (schild@nceas.ucsb.edu)
    Eric Fegraus (fegraus@nceas.ucsb.edu)
    Matthew Brooke (brooke@nceas.ucsb.edu)
    Jing Tao (tao@nceas.ucsb.edu)

This is Morpho, a data management application for managing ecological data.  Morpho allows researchers to describe their data using a comprehensive and flexible metadata specification, and to share their data publicly or to specific collaborators over the Knowledge Netwrok for Biocomplexity system. Although Morpho can be used without an account on the KNB, it is free and simple to obtain one: just register at

 http://knb.ecoinformatics.org.

Morpho's main features include:
  1. Flexible metadata creation and editing
  2. Compliance with Ecological Metadata Language
  3. Powerful metadata search for data on the network or locally
  4. Comprehensive revision control for changing data and metadata
  5. Easy-to-use collaboration features via the KNB system

Further documentation is available under the help menu in Morpho (after it has been installed), or in the "docs"subdirectory of the Morpho installation (after it has been installed), or on the KNB website  (http://knb.ecoinformatics.org/software/morpho/).

INSTALLATION
------------
1. Uninstall old versions of Morpho.
Windows may get confused when there are multiple versions of Morpho installed, so we recommend that you uninstall previous version using the Windows control panel "Add/Remove Programs" before proceeding with the installation.

2. Download the new version, along with Java. Morpho is a Java application. One thus needs to have a Java Virtual  Machine (JVM) installed on the machine running Morpho, preferably  version 1.3 or later.  Morpho can be downloaded with Java for your convenience, or it can be obtained from Sun (http://java.sun.com). With Java the Morpho distribution is a much larger download, so if you already have Java 1.3 or later installed, its better to download the Morpho-only distribution. [Note: Java 1.4 has now been released by Sun. Morpho appears to work with this new release, but has not been exhaustively tested.]

3. Run the installer application
Download the installation executable and run it on your desired platform. For windows this requires simply double-clicking on the executable file that you downloaded, but may vary depending on your operating system.

STARTING MORPHO
---------------
Once Morpho has been installed, it can be launched using a shortcut, as follows:

    Windows:   Select the "morpho" shortcut found under the windows "Start menu". (You can also use the "morpho.bat" file found in the main Morpho directory.)

    Macintosh: Double click on the morpho icon.

    Linux: Run the "morpho" shell script from the instalation directory

If this is the first time you have launched Morpho, you will see a "New Profile" dialog box.  Profiles are Morpho's way of keeping your data separate from other people's data on your computer in case more than one person uses Morpho on your computer.  Simply fill in the forms and the profile will be created for you.

When asked for information about your KNB Metacat account, simply fill in your username and organization from your KNB account.  If you do not have a  KNB account you can get one at the KNB website (http://knb.ecoinformatics.org) by filling out a simple registration form.  This account allows you to  collaborate with other researchers and share and exchange data securely.

Further help information, including a tutorial, can be found in the Morpho "Help..." menu once you can see the main screen.

The Knowledge Network for Biocomplexity (KNB)
---------------------------------------------
Morpho is one of a series of tools that are being built for the KNB in order to make ecological data and metadata more accessible and useful to scientists. In particular, the KNB has a distributed metadata catalog called Metacat. Morpho can be used to contribute data and metadata to the KNB and make it accessible in the Metacat system.  To do so, one must have an account on the KNB, which can be obtained for testing purposes by writing to "knb-software@nceas.ucsb.edu".

See the KNB website (http://knb.ecoinformatics.org/software) for more information and other software tools.

Known Bugs and Feature Requests
-------------------------------
See http://bugzilla.ecoinformatics.org where we maintain a comprehensive list of the know issues with Morpho, and a list ofthe new features that have been requested.  Submit reports of new bugs to this address as well.


Version History
-------------------------------

Version 1.1.1 corrects several minor problems in Version 1.1.0 and adds the capability to open previous versions of packages. Previously only the most recent version could be opened. The user can open previous versions using a new menu item in the popup menu that appears when one 'right-clicks' on a row in the table displaying the user's data or the results of a query.

Version 1.1.0 has a substantial number of changes from previous versions of Morpho, although most of these changes are internal and do not appear in the visual interface. In general, stability has been increased and the ability to handle large metadata and data files enhanced by reduction of unneccessary in-memory data/metadata storage. Also, many time-consuming tasks have been moved to independent threads so that Morpho is responsive to the user while these tasks are being carried out. (A 'flapping butterfly' icon is used to indicate that these background tasks are underway.)

The TextImportWizard layout has been changed slightly to make it more obvious what metadata is required for various columns in the data table. Also, the capability for viewing data has been enhanced with a table-like viewer and the added capability to directly view images. Data in a package can also now be editied/updated from within Morpho.
---
Version 1.0.6 fixes an error in the editor which did not allow deletion/creation of certain elements in eml-access documents.
---
Version 1.0.5 of Morpho fixes some additional bugs in the editor, especially with CHOICE nodes. Changes for speeding up several function have also been made. Also inceased the memory the JVM asks for to 128M from 64M. 
---
Version 1.0.4 of Morpho contains several more fixes to errors in the Morpho  editor. In particular, empty nodes were being trimmed incorrectly and sequences of nodes inside choice elements were handled incorrectly.
---
Version 1.0.3 of Morpho fixes several errors in the Morpho editor which caused invalid XML documents to be created. These caused problems when documents were edited and then submitted to Metacat.
---
Version 1.0.2 of Morpho is another bug-fix. In particular, problems in handling large data files have been fixed and non-scrolling lists have been corrected to scroll properly
---
Version 1.0.1 of Morpho is bug-fix. Specifically, it fixes one problem with the editor that occurred when multiple empty nodes are displayed. Previously, when data was entered in one node, it also appeared in all copies of the node. A second bug occurred in the TextImportWizard when one attempted to insert very large files. The user is now warned and the import truncated. The user should note that minimum and maximum values may not be correct when the import is trucated.

A bug that has not yet been fixed sometimes occurs in the TextImportWizard - Step 3. When on selects a column and then tries to enter metadata about that column by clicking on a text field, the caret that indicates that a text field is selected sometimes does not appear. Despite the appearance that the text field is not selected, one can type keystrokes and the characters should appear in the text field.
    

 
 


Notes for Windows Users
-----------------------
With some video cards, Java windows will sometimes not be displayed properly. In particular, scroll windows may appear garbled or overwritten. Turning off graphics acceleration corrects the problem on some machines. On others, the  user can simple click in the window or resize it slightly to force a display update.

Notes for Macintosh Users
-------------------------
There are two versions of Morpho available for use with Macintosh computers. One version is for the new OS X operating system and the other is for the older OS 8 and OS 9 systems. This difference is due to the fact that newer versions of the Java Virtual Machine used to execute Morpho are not available for the older Macintosh operating system. For Macintosh systems older than OS X, only Java 1.18 is available, while Java 1.3 is included as part of the Mac OS X installation. 

Most of Morpho works with the older versions of Java. (Exceptions include some key stroke equivalents for some menu commands, and the ability to save and re-execute queries from menus. There may also be some memory limitations when  working with large amounts of data/metadata.) However, Morpho is 2-3 times faster when run under OS X using the newer Java Virtual Machine! For that reason alone, IT IS RECOMMENDED THAT THE OS X VERSION OF MORPHO SHOULD BE USED WHENEVER POSSIBLE.   

Also, on the Macintosh, pressing the <Control> key down while clicking is the equivalent of a click of the right mouse button under Windows. Thus "<Control>+ Click" should be used to bring up popup menus when running Morpho on a  Macintosh.

Other Credits:
--------------
Icons used in this software were originally designed by other parties, and
have be modified for Morpho.  They include incons from the IconFactory's World
of Aqua series (http://www.iconfactory.com), and icons from Everaldo Coelho's
Conectiva Crystal Series (http://www.everaldo.com).  Please see the 
licenses/ICONREADME.txt for more information about the IconFactory icons.  


Legalese
--------
This software is copyrighted by The Regents of the University of California and the National Center for Ecological Analysis and Synthesis and licensed under the GNU GPL; see the 'LICENSE' file for details.

This material is based upon work supported by the  National Science Foundation under Grant No. DEB99-80154 and DBI99-04777. Any opinions, findings and conclusions or recomendations expressed in this material are those of the author(s) and do not necessarily reflect  the views of the National Science Foundation (NSF).
