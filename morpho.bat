#
# script for launching morpho from windows systems
#
# '$RCSfile: morpho.bat,v $'
# '$Author: brooke $'
# '$Date: 2002-09-24 17:17:45 $'
# '$Revision: 1.17 $'

SET  XMLP=lib\xercesImpl.jar
SET   API=lib\xml-apis.jar
SET XALAN=lib\xalan.jar
SET   DMC=lib\morpho.jar
SET   CFG=lib\morpho-config.jar
SET   JLF=lib\jlfgr-1_0.jar
SET   HTP=lib\httpclient.jar
SET   ITS=lib\itislib.jar
SET   JSE=lib\jsse.jar
SET   JCE=lib\jcert.jar
SET   JNE=lib\jnet.jar
SET   LAF=lib\kunststoff.jar

SET CPATH=.;%XMLP%;%API%;%XALAN%;%DMC%;%CFG%;%JLF%;%HTP%;%ITS%;%JSE%;%JCE%;%JNE%;%LAF%

java -Xmx512m -Xss1m -cp %CPATH% edu.ucsb.nceas.morpho.Morpho