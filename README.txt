# 
#  '$RCSfile: README.txt,v $'
#   '$Author: berkley $'
# '$Revision: 1.10 $'
#     '$Date: 2001-07-30 16:49:44 $'
#

Morpho: XML Metadata Client
---------------------------

Version: 1.0.0beta1 Release - Aug 1, 2001

Feedback and bugs to: knb-software@nceas.ucsb.edu
                      http://bugzilla.ecoinformatics.org

Contributors: Matt Jones (jones@nceas.ucsb.edu)
              Dan Higgins (higgins@nceas.ucsb.edu)
              Chad Berkley (berkley@nceas.ucsb.edu)

This is a BETA version of the Morpho client tool. It is known to be incomplete
and to have a number of bugs. It is being released simply for preliminary
review. 

It is almost a complete change from previous versions - thus, previous help 
documentation is NOT applicable. 

Morpho is a Java application. One thus needs to have a Java Virtual Machine (JVM)
installed on the machine running Morpho. You can see if a JVM is installed by
typing 'java' on the command line and seeing if any messages appear. If Java is
not installed, a Java runtime system can be downloaded from Sun at
"http://java.sun.com/j2se/1.3/jre"

The current version of Morpho has only been tested on Windows and Linux OSs using 
Java version 1.3. It is not expected to run on Macs that do not have OS X (and it
has not been tested even on OS X machines).

If Java is properly installed, you should be able to start Morpho by simply
executing the 'morpho.bat' file in Windows or the 'morpho' script on Linux.

The Knowledge Network for Biocomplexity (KNB)
---------------------------------------------
Morpho is one of a series of tools that are being built for the KNB in order
to make ecological data and metadata more accessible and useful to scientists.
In particular, the KNB has a distributed metadata catalog called Metacat.
Morpho can be used to contribute data and metadata to the KNB and make it
accessible in the Metacat system.  To do so, one must have an account on
the KNB, which can be obtained for testing purposes by writing to
"knb-software@nceas.ucsb.edu".

See the KNB website (http://knb.ecoinformatics.org/software) for more information and
other software tools.

Known Bugs (see http://bugzilla.ecoinformatics.org)
---------------------------------------------------------
244 need refresh mechanism for query screen  
255 Migrate to EML 2 beta DTDs for all Morpho uses 
202 new config management/profile feature for morpho framework 
250 add additional attribute metadata to display 
251 add return and escape defaults on wizard 
252 reformat table entity display 
253 use profile info for default text in wizard 
260 acl handling for packages needs to be updated 
116 need data set parser module 
115 need short-term dataset parser implementation 
212 jar file handling for data packages 
217 need simple spatial search 
201 add https support to client framework 
088 need ability to manipulate access control lists 
166 Update options handling
206 need help system for morpho 
211 Data entity editor and display 
236 improve handling of query options 
239 search dialog allows user to uncheck both catalog and local 
254 Look up id sequence on morpho startup 
123 saved resultsets, cached documents 
205 cut/copy/paste 
165 taxonomic search facility 
256 allow toolbar to display alternative widgets




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
