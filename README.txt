Morpho: XML Metadata Client
---------------------------

Version: 1.0.5 - February 19, 2002

Feedback and bugs to: knb-software@nceas.ucsb.edu
                      http://bugzilla.ecoinformatics.org

Contributors: Matt Jones (jones@nceas.ucsb.edu)
              Dan Higgins (higgins@nceas.ucsb.edu)
              Chad Berkley (berkley@nceas.ucsb.edu)
              Jivka Bojilova (bojilova@nceas.ucsb.edu)
              Chris Jones (cjones@lifesci.ucsb.edu)
              Mark Schildhauer (schild@nceas.ucsb.edu)
              Eric Fegraus (fegraus@nceas.ucsb.edu)

This is Morpho, a data management application for managing ecological
data.  Morpho allows researchers to describe their data using a comprehensive
and flexible metadata specification, and to share their data publicly or
to specific collaborators over the Knowledge Netwrok for Biocomplexity system.
Although Morpho can be used without an account on the KNB, it is free and
simple to obtain one: just register at http://knb.ecoinformatics.org.

Morpho's main features include:
  1. Flexible metadata creation and editing
  2. Compiance with Ecological Metadata Language
  3. Powerful metadata search for data on the network or locally
  4. Comprehensive revision control for changing data and metadata
  5. Easy-to-use collaboration features via the KNB system

Further documentation is available under the help menu in Morpho (after it
has been installed), or in the "docs"subdirectory of the Morpho installation
(after it has been installed), or on the KNB website 
(http://knb.ecoinformatics.org/software/morpho/).

INSTALLATION
------------
1. Uninstall old versions of Morpho.
Windows may get confused when there are multiple versions of Morpho installed,
so we recommend that you uninstall previous version using the Windows control
panel "Add/Remove Programs" before proceeding with the installation.

2. Download the new version, along with Java.
Morpho is a Java application. One thus needs to have a Java Virtual 
Machine (JVM) installed on the machine running Morpho, preferably 
version 1.3 or later.  Morpho can be downloaded with Java for your 
convenience, or it can be obtained from Sun (http://java.sun.com).
With Java the Morpho distribution is a much larger download, so if you
already have Java 1.3 or later installed, its better to download the
Morpho-only distribution.

3. Run the installer application
Download the installation executable and run it on your desired platform.
For windows this requires simply double-clicking on the executable file that
you downloaded, but may vary depending on your operating system.

STARTING MORPHO
---------------
Once Morpho has been installed, it can be launched using a shortcut, as
follows:

    Windows:   Select the "morpho" shortcut found under the windows
               "Start menu".
    Macintosh: Double click on the morpho icon.
    Linux:     Run the "morpho" shell script from the instalation directory

If this is the first time you have launched Morpho, you will see a "New
Profile" dialog box.  Profiles are Morpho's way of keeping your data separate
from other people's data on your computer in case more than one person uses
Morpho on your computer.  Simply fill in the forms and the profile will be
created for you.

When asked for information about your KNB Metacat account, simply fill in your
username and organization from your KNB account.  If you do not have a 
KNB account you can get one at the KNB website (http://knb.ecoinformatics.org)
by filling out a simple registration form.  This account allows you to 
collaborate with other researchers and share and exchange data securely.

Further help information, including a tutorial, can be found in the Morpho
"Help..." menu once you can see the main screen.

The Knowledge Network for Biocomplexity (KNB)
---------------------------------------------
Morpho is one of a series of tools that are being built for the KNB in order
to make ecological data and metadata more accessible and useful to scientists.
In particular, the KNB has a distributed metadata catalog called Metacat.
Morpho can be used to contribute data and metadata to the KNB and make it
accessible in the Metacat system.  To do so, one must have an account on
the KNB, which can be obtained for testing purposes by writing to
"knb-software@nceas.ucsb.edu".

See the KNB website (http://knb.ecoinformatics.org/software) for more 
information and other software tools.

Known Bugs and Feature Requests
-------------------------------
See http://bugzilla.ecoinformatics.org where we maintain a comprehensive
list of the know issues with Morpho, and a list ofthe new features that
have been requested.  Submit reports of new bugs to this address as well.
---
Version 1.0.1 of Morpho is bug-fix. Specifically, it fixes one problem with
the editor that occurred when multiple empty nodes are displayed. Previously,
when data was entered in one node, it also appeared in all copies of the
node. A second bug occurred in the TextImportWizard when one attempted
to insert very large files. The user is now warned and the import truncated.
The user should note that minimum and maximum values may not be correct when
the import is trucated.

A bug that has not yet been fixed sometimes occurs in the TextImportWizard -
Step 3. When on selects a column and then tries to enter metadata about that
column by clicking on a text field, the caret that indicates that a text field
is selected sometimes does not appear. Despite the appearance that the text
field is not selected, one can type keystrokes and the characters should appear
in the text field.
---
Version 1.0.2 of Morpho is another bug-fix. In particular, problems in handling
large data files have been fixed and non-scrolling lists have been corrected
to scroll properly    
---
Version 1.0.3 of Morpho fixes several errors in the Morpho editor which caused
invalid XML documents to be created. These caused problems when documents were
edited and then submitted to Metacat.
---
Version 1.0.4 of Morpho contains several more fixes to errors in the Morpho 
editor. In particular, empty nodes were being trimmed incorrectly and sequences
of nodes inside choice elements were handled incorrectly. 
---
Version 1.0.5 of Morpho fixes some additional bugs in the editor, especially
with CHOICE nodes. Changes for speeding up several function have also been made.
Also inceased the memory the JVM asks for to 128M from 64M.

Legalese
--------
This software is copyrighted by The Regents of the University of California
and the National Center for Ecological Analysis and Synthesis
and licensed under the GNU GPL; see the 'LICENSE' file for
details.

This material is based upon work supported by the 
National Science Foundation under Grant No. DEB99-80154 and DBI99-04777.
Any opinions, findings and conclusions or recomendations expressed in this
material are those of the author(s) and do not necessarily reflect 
the views of the National Science Foundation (NSF).
