# How to build

* Install JDK from java.sun.com
* set env variable JAVA_HOME to the installed JDK
* run from project folder: "mvnw clean package"

# How to run

* Launch start.bat (first time will apply an export script)
* Launch DCS

# Available settings from start.bat

Look into the start.bat for the example line, and change the last one.

* -noborders remove window borders (still draggable via mouse)
* -useMFD1 connects to the thrustmaster MFD buttons, number 1
* -useMFD2 connects to the thrustmaster MFD buttons, number 2
* -xy2433,25 Initial position
* -s512 Size in pizels (square, both dimensions)