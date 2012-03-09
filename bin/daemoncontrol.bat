@echo off
rem TahitiDaemon control script

set AGLET_HOME=%~dp0..

set LOCALCLASSPATH=lib;lib\classes;lib\*;%CLASSPATH%

cd "%AGLET_HOME%"

java ^
    -classpath "%LOCALCLASSPATH%" ^
    com.ibm.aglets.tahiti.TahitiDaemonClient ^
    %1 %2 %3 %4 %5 %6 %7 %8 %9
