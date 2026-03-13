@echo off
set CP=lib\javafx-controls-17.0.6.jar;lib\javafx-controls-17.0.6-win.jar;lib\javafx-graphics-17.0.6.jar;lib\javafx-graphics-17.0.6-win.jar;lib\javafx-base-17.0.6.jar;lib\javafx-base-17.0.6-win.jar;lib\javafx-fxml-17.0.6.jar;lib\javafx-fxml-17.0.6-win.jar
java -Djava.net.preferIPv4Stack=true -cp "target\classes;lib\*" client.App
