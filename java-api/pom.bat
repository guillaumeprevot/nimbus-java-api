@echo off
set PATH=%PATH%;%cd:~0,1%:\apps\openjdk-11\bin
set PATH=%PATH%;%cd:~0,1%:\apps\apache-maven-3.6.3\bin
mvn compile
