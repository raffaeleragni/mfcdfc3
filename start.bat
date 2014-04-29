@echo off
set JAVA_HOME=jre8
set PATH=%PATH%;%JAVA_HOME%\bin
rem start javaw -Djava.library.path=target/natives -jar target/mfcdfc3-1.0-jar-with-dependencies.jar -noborders -useMFD1 -xy1920,610 -s512
start javaw -Djava.library.path=target/natives -jar target/mfcdfc3-1.0-jar-with-dependencies.jar -noborders -useMFD2 -xy2440,10 -s512
