@echo off
java -Djava.net.preferIPv4Stack=true -cp "target\classes;lib\*" server.Server
pause
