@echo off
REM # 
REM # '$RCSfile: morpho.bat,v $'
REM # '$Author: tao $'
REM # '$Date: 2002-11-21 01:36:18 $'
REM # '$Revision: 1.23 $'
REM # 
REM #  Script for launching morpho from windows systems
REM #


SET  XMLP=lib\xercesImpl.jar
SET   API=lib\xml-apis.jar
SET XALAN=lib\xalan.jar
SET   DMC=lib\morpho.jar
SET   CFG=lib\morpho-config.jar
SET   JLF=lib\shippedIcons.jar
SET   JHELP=lib\jhall.jar
SET   HELP=lib\holidays.jar
SET   HTP=lib\httpclient.jar
SET   ITS=lib\itislib.jar
SET   JSE=lib\jsse.jar
SET   JCE=lib\jcert.jar
SET   JNE=lib\jnet.jar
SET   LAF=lib\kunststoff.jar  
SET   LMP=lib\liveMap.jar


SET CPATH=.;%LMP%;%XMLP%;%API%;%XALAN%;%DMC%;%CFG%;%JLF%;%JHELP%;%HTP%;%ITS%;%JSE%;%JCE%;%JNE%;%LAF%;%HELP%

echo ----------------------------------

java -version

echo ----------------------------------

java -Xmx512m -Xss1m -cp %CPATH% edu.ucsb.nceas.morpho.Morpho

