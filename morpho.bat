@echo off
REM # 
REM # '$RCSfile: morpho.bat,v $'
REM # '$Author: brooke $'
REM # '$Date: 2002-09-26 02:50:20 $'
REM # '$Revision: 1.20 $'
REM # 
REM #  Script for launching morpho from windows systems
REM #


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

echo ----------------------------------

java -version

echo ----------------------------------

java -Xmx512m -Xss1m -cp %CPATH% edu.ucsb.nceas.morpho.Morpho

