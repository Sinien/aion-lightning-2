@echo off
title Aion-Lightning Chat Server Console
:start
echo Starting Aion-Lightning Chat Server.
echo.
REM -------------------------------------
REM Default parameters for a basic server.
java -Xms128m -Xmx128m -ea -cp ./libs/*;al-cserver-1.0.0.jar com.aionemu.chatserver.ChatServer
REM
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%


if ERRORLEVEL 1 goto error
goto end
:error
echo.
echo Chat Server Terminated Abnormally, Please Verify Your Files.
echo.
:end
echo.
echo Chat Server is terminated...
echo.
pause