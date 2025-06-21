@echo off
set PATH=%PATH%;%cd:~0,1%:\apps\openjdk-21\bin
set PATH=%PATH%;%cd:~0,1%:\apps\apache-maven\bin
mvn compile
