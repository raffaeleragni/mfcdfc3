@echo off
set JAVA_HOME=jre8
set PATH=%PATH%;%JAVA_HOME%\bin
REM mvn pacakge
start javaw -Djava.library.path=target\natives -jar target\mfcdfc3-1.0-jar-with-dependencies.jar