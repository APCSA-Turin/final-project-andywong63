@echo off

chcp 65001
mvn compile exec:java -Dexec.mainClass="com.example.Server"