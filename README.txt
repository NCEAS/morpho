Morpho: XML Metadata Client  
---------------------------

Version: 1.8.0RC1

Feedback and bugs to: morpho-dev@ecoinformatics.org
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
    Christy Bowles (bowles@nceas.ucsb.edu)
    Saurabh Garg (sgarg@nceas.ucsb.edu)
    Perumal Sambasivam (sambasiv@nceas.ucsb.edu)
    Andrea Chadden (chadden@nceas.ucsb.edu)
    Rick Reeves (reeves@nceas.ucsb.edu)
    Sandy Andelman (andelman@nceas.ucsb.edu)
    P. Mark Anderson (anderson@nceas.ucsb.edu)
    Callie Bowdish (bowdish@nceas.ucsb.edu)
    Veronique Connolly (connolly@nceas.ucsb.edu)
    Will Tyburczy (tyburczy@nceas.ucsb.edu)
    Jim Regetz(regetz@nceas.ucsb.edu)
    Judith Kruger(JudithW@sanparks.org)
    Margaret O'Brien(mob@icess.ucsb.edu)
    
This is Morpho, a data management application for managing ecological data.
Morpho allows researchers to describe their data using a comprehensive and
flexible metadata specification, and to share their data publicly or to
specific collaborators over the Knowledge Network for Biocomplexity system.
Although Morpho can be used without an account on the KNB, it is free and
simple to obtain one: just register at:

 http://knb.ecoinformatics.org

Morpho's main features include:
  1. Flexible metadata creation and editing
  2. Compliance with Ecological Metadata Language
  3. Powerful metadata search for data on the network or locally
  4. Comprehensive revision control for changing data and metadata
  5. Easy-to-use collaboration features via the KNB system      

**************************************************
ATTENTION ! --  ATTENTION !  -- ATTENTION !  (April 15, 2004)


Many sections of Morpho Version 1.5 (and subsequent versions) have been completely rewritten to work with
EML 2.0.0 (and newer versions of EML). EML 2.0.0 is a considerable change from
the so-called "Beta6" version used in previous versions of Morpho. Previously, eml
modules were separate documents linked by references to IDs in a set of "triples".
In EML 2.0.0, all metadata is stored in a single XML document. This change resulted
in the need for considerable changes in the Morpho code.

A significant effort has also been spent in creating a new Data Package Wizard to
make it easier for a user to create a package without learning all the details of
EML 2.0.0. The user can also edit many of the features of a document using forms
from the Data Package Wizard called up from the Documentation menu.




**************************************************

Further documentation is available under the help menu in Morpho (after it has been installed), or in the "docs"subdirectory of the Morpho installation (after it has been installed), or on the KNB website  (http://knb.ecoinformatics.org/software/morpho/).

INSTALLATION
------------
1. Uninstall old versions of Morpho.
Windows may get confused when there are multiple versions of Morpho installed, so we recommend that you uninstall previous version using the Windows control panel "Add/Remove Programs" before proceeding with the installation. Alternately, you may uninstall Morpho by running Morpho Uninstaller.
Note, however, that previously created data packages will NOT be deleted because they are stored in a separate directory. Older packages will contnue to be available when new versions of Morpho are installed.

2. Download a JVM if necessary. 
Morpho and the installers are Java applications. One thus needs to have a Java Virtual  Machine (JVM) installed on the machine that will install and run Morpho, preferably  version 1.5 or later.  If you don't have JVM on the machine, the installers will help you to get one from the Internet.

3. Run the installer.
Download and run the installation executable. This process will be different depending on your operating system. For windows and mac, simply double-click on the executable file that you downloaded. For Linux, run the following command from a terminal window: java -jar morpho-1.8.0-linux.jar

STARTING MORPHO
---------------
Once Morpho has been installed, it can be launched using a shortcut, as follows:

    Windows:   Select the "morpho" shortcut found under the windows "Start menu". (You can also use the "morpho.bat" file found in the main Morpho directory.)

    Macintosh: Double click on the morpho icon.

    Linux: Run the "morpho" shell script from the installation directory

If this is the first time you have launched Morpho, you will see a "New Profile" dialog box.  Profiles are Morpho's way of keeping your data separate from other people's data on your computer in case more than one person uses Morpho on your computer.  Simply fill in the forms and the profile will be created for you.

When asked for information about your KNB Metacat account, simply fill in your username and organization from your KNB account.  If you do not have a  KNB account you can get one at the KNB website (http://knb.ecoinformatics.org) by filling out a simple registration form.  This account allows you to  collaborate with other researchers and share and exchange data securely.

Further help information, including a tutorial, can be found in the Morpho "Help..." menu once you can see the main screen. [You can also use a browser to open the file "index.html" which is in the "docs/user/" subdirectory.]

The Knowledge Network for Biocomplexity (KNB)
---------------------------------------------
Morpho is one of a series of tools that are being built for the KNB in order to make ecological data and metadata more accessible and useful to scientists. In particular, the KNB has a distributed metadata catalog called Metacat. Morpho can be used to contribute data and metadata to the KNB and make it accessible in the Metacat system.  To do so, one must have an account on the KNB, which can be obtained for testing purposes by writing to "knb-software@nceas.ucsb.edu".

See the KNB website (http://knb.ecoinformatics.org/software) for more information and other software tools.

Known Bugs and Feature Requests
-------------------------------
See http://bugzilla.ecoinformatics.org where we maintain a comprehensive list of the know issues with Morpho, and a list of the new features that have been requested.  Submit reports of new bugs to this address as well.



Version History
-------------------------------
***
Version 1.8.0 is a major update to 1.7 with new features and bugs fixed.  The new features are that Morpho supports user saving and opening a unfinished New Data Package/Entity wizard, recovers the crashed New Data Package/Entity wizard, imports the external EML file to Morpho, and exports EML document to Biological Data Profile format file.

***
Version 1.7 is a major update to 1.6.1 with bug fixed and new features. The new features are that Morpho now supports EML 2.1.0, connects Metacat through SSL, seperates access rules of entities from the package itself and backups user data. This release also includes bug fixes for, among other things, dealing with docid conflicts that may occur during the saving process, and for correcting local invalid eml 201 documents.

***
Version 1.6.1 is a minor update to 1.6.0 with bug fixed only. One major bug fixed was the prompt user to convert eml 2.0.0 packages to eml 2.0.1. The other bugs fixes includes tree editor, changing morpho example package to eml 2.0.1 and data file couldn't update revision number et al.

***
Version 1.6.0 is a major update to 1.5.1 with bug fixes and new features. One of the major features is that Morpho now supports EML 2.0.1. All the Data Package Wizard screens are now accessible from the Documentation Menu. This release also includes bug fixes in the tree editor, the text import wizard, and the new data package wizard, as well as improved performance times for loading large data tables.

***
Version 1.5.1 is a minor update to 1.5.0 with bug fixes only. One major bug fixed was the inability of version 1.5.0 to locally save data files from a server originally submitted as part of a previously created "beta6" eml package.

***
Version 1.5

Version 1.5 is a major update from previous versions with numerous changes in both interface features and internal logical structure. Many of these changes were made in providing full support for EML 2.0.0 which is considerably changed from the eml version previously supported (Beta 6). [Note that older eml packages are automatically converted to EML 2.0.0 and displayed as such. Actual conversion of the package will only occur when the owner saves the converted data.]

This version also has a completely new Wizard for creating new data packages. This wizard also allows one to use the wizard forms for editing some parts of existing documents.

***
Version 1.4

Version 1.4 of Morpho adds two new features as well as a number of speed enhancements to the previous version. 

There is now a "Preferences" menu item in the File menu. This menu item brings up a dialog box which allows the user to change the URL of the network server (i.e. the Metacat server) that is being used, to set the debug level and logging to file options, and to change the visual "look-and-feel" used by Morpho. These options have always been set in the Morpho "config.xml" file, but now they can be set directly from the Morpho application.    

Secondly, the "Export" menu item now displays a dialog that allows the user to export a package to a directory, to a zip file, or to a EML 2.0.0 format file. The EML 2.0.0 format is a single eml file that contains a combination of the multiple eml-beta6 documents that are created by Morpho. The "triples" of earlier versions of eml are replaced by the single document structure of EML 2.0.0.
 

***
Version 1.3

Several changes to the visual interface have been made in this version. The initial screen that appears when Morpho is first launched has been substantially changed. In addition, the text import wizard that is used to collect metadata from existing data sets has been extensively modified. In addition, spatial query functionality has been implemented using a map-based interface to "draw" a bounding box. (Data packages with geographic entries indicating that the data is related to regions within this bounding box are returned.) The Help system has also been modified to provide search capability and additional information on eml elements.

Older datasets may have some metadata modules which are not explicitly associated with an access control module through "triples" in the dataset module. In this case, only the owner has access to these modules. When these packages are opened by the owner, a dialog will appear notifying the owner. The dialog also provides the option to correct the package and provide an access control associaton for all modules in the package. This will make all pieces of the package available according to the rules in the access control module. (This is the default for any newly created packages.)

*** 
Version 1.2

Version 1.2 represents a substantial change in the visual interface from previous versions. A datacentric view of datapackages is shown. This view includes a tabular display of a data table along with descriptive information about the data. The data and descriptions are contained in resizable panels. The user can thus enlarge parts of the window of special interest as desired. Menus have also been redesigned and, in some cases, renamed. Rather than presenting a default list of owner documents, the default window is empty and the user needs to use the "Open" menu or toolbar button to get a list of user owned documents as is customary in most applications.

If you have used a previous version of Morpho, then you may have local datapackages stored in a "profiles" directory inside the main Morpho directory. The location where the "profiles" directory is created and accessed in Version 1.2 has been changed from the Morpho install directory to a subdirectory within the users "home" directory. [On Windows 2000 or XP machines, the home directory is named after the user name (e.g. "higgins") and is located inside the "Documents and Settings" folder, usually under C:\] A directory named ".morpho" is created inside the user's home directory and the new profiles directory is created there when Morpho is first executed. To access previouly created datapackages, copy those packages from your existing profiles directory to the profiles directory inside the <home>/.morpho directory.


***
Version 1.1.1 corrects several minor problems in Version 1.1.0 and adds the capability to open previous versions of packages. Previously only the most recent version could be opened. The user can open previous versions using a new menu item in the popup menu that appears when one "right-clicks" on a row in the table displaying the user's data or the results of a query.

Version 1.1.0 has a substantial number of changes from previous versions of Morpho, although most of these changes are internal and do not appear in the visual interface. In general, stability has been increased and the ability to handle large metadata and data files enhanced by reduction of unneccessary in-memory data/metadata storage. Also, many time-consuming tasks have been moved to independent threads so that Morpho is responsive to the user while these tasks are being carried out. (A "flapping butterfly" icon is used to indicate that these background tasks are underway.)

The Text Import Wizard layout has been changed slightly to make it more obvious what metadata is required for various columns in the data table. Also, the capability for viewing data has been enhanced with a table-like viewer and the added capability to directly view images. Data in a package can also now be editied/updated from within Morpho.
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
There are two versions of Morpho available for use with Macintosh computers. One version is for the new OS X operating system and the other is for the older OS 8 and OS 9 systems. This difference is due to the fact that newer versions of the Java Virtual Machine used to execute Morpho are not available for the older Macintosh operating system. For Macintosh systems older than OS X, only Java 1.18 is available, while Java 1.4 is included as part of the Mac OS X installation. 

The latest version of Morpho will only execute on Macintosh systems using OS X.  IT IS RECOMMENDED THAT THE OS X VERSION OF MORPHO SHOULD BE USED WHENEVER POSSIBLE, since only obsolete versions are available for the older operationg systems.   

Also, on the Macintosh, pressing the <Control> key down while clicking is the equivalent of a click of the right mouse button under Windows. Thus "<Control>+ Click" should be used to bring up popup menus when running Morpho on a Macintosh.

Other Credits:
--------------
Icons used in this software were originally designed by other parties, and
have be modified for Morpho.  They include incons from the IconFactory's World
of Aqua series (http://www.iconfactory.com), and icons from Everaldo Coelho's
Conectiva Crystal Series (http://www.everaldo.com).  Please see the 
licenses/ICONREADME.txt for more information about the IconFactory icons.  


Legalese
--------
This software is copyrighted by The Regents of the University of California and the National Center for Ecological Analysis and Synthesis and licensed under the GNU GPL; see the "LICENSE" file for details.

This material is based upon work supported by the  National Science Foundation
under Grant No. DEB99-80154, DBI99-04777, and DBI01-31178. Any opinions, findings and conclusions or recomendations expressed in this material are those of the author(s) and do not necessarily reflect  the views of the National Science Foundation (NSF).
