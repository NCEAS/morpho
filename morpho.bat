@echo off
SetLocal EnableDelayedExpansion
REM # 
REM # '$RCSfile: morpho.bat,v $'
REM # '$Author: leinfelder $'
REM # '$Date: 2008-11-22 01:28:10 $'
REM # '$Revision: 1.29 $'
REM # 
REM #  Script for launching morpho from windows systems
REM #

SET CPATH=.

FOR %%i in (lib\*.jar) DO SET CPATH=!CPATH!;%%i

echo %CPATH%

echo ----------------------------------

java -version

echo ----------------------------------

java -Xmx512m -Xss1m -cp %CPATH% edu.ucsb.nceas.morpho.Morpho

EndLocal