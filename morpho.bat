@echo off
REM # 
REM # script for launching morpho from windows systems
REM #
REM # '$RCSfile: morpho.bat,v $'
REM # '$Author: cjones $'
REM # '$Date: 2002-09-26 01:57:51 $'
REM # '$Revision: 1.19 $'

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

echo CLASSPATH = %CPATH%
java -Xmx512m -Xss1m -cp %CPATH% edu.ucsb.nceas.morpho.Morpho
