@echo off
REM # 
REM # '$RCSfile: runnit_Memtest.bat,v $'
REM # '$Author: higgins $'
REM # '$Date: 2003-08-22 15:40:45 $'
REM # '$Revision: 1.1 $'
REM # 
REM #  Script for launching morpho from windows systems 
REM #  This batch file uses the MemoryMonitor class to launch Morpho
REM #  Memory useage is displayed in a separate window
REM #


SET  XMLP=lib\xercesImpl.jar
SET   API=lib\xml-apis.jar
SET XALAN=lib\xalan.jar
SET   DMC=lib\morpho.jar
SET   CFG=lib\morpho-config.jar
SET   JLF=lib\shippedIcons.jar
SET   JHELP=lib\jhall.jar
SET   HELP=lib\morphohelp.jar
SET   HTP=lib\httpclient.jar
SET   ITS=lib\itislib.jar
SET   JSE=lib\jsse.jar
SET   JCE=lib\jcert.jar
SET   JNE=lib\jnet.jar
SET   LAF=lib\kunststoff.jar
SET   LMP=lib\liveMap.jar


SET CPATH=.;%LMP%;%XMLP%;%API%;%XALAN%;%DMC%;%CFG%;%JLF%;%JHELP%;%HTP%;%ITS%;%JSE%;%JCE%;%JNE%;%LAF%;%HELP%;%JHELP%

echo ----------------------------------

java -version

echo ----------------------------------

java -Xmx512m -Xss1m -cp %CPATH% MemoryMonitor edu.ucsb.nceas.morpho.Morpho

