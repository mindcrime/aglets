@echo off
rem Aglets Server startup script

set AGLET_HOME=%~dp0..

set LOCALCLASSPATH=lib;lib\classes;lib\*;%CLASSPATH%

:loop

cd "%AGLET_HOME%"
java ^
    -Daglets.home="." ^
    -classpath "%LOCALCLASSPATH%" ^
    com.ibm.awb.launcher.Main ^
    -f "cnf\aglets.props" ^
    %1 %2 %3 %4 %5 %6 %7 %8 %9

if errorlevel 1 goto exit
goto loop

:exit
