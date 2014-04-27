@echo off
set JAVA_HOME=jre8
set PATH=%PATH%;%JAVA_HOME%\bin
start javaw -Djava.library.path=target/natives -jar target/mfcdfc3-1.0-jar-with-dependencies.jar -noborders -useMFD1 -xy1920,625
start javaw -Djava.library.path=target/natives -jar target/mfcdfc3-1.0-jar-with-dependencies.jar -noborders -useMFD2 -xy2455,20
