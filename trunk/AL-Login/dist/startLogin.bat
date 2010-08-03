@echo off
title Aion-Lightning Login Server Console
:start
echo Starting Aion-Lightning Login Server.
echo.
REM -------------------------------------
REM Default parameters for a basic server.
java -Xms8m -Xmx32m -ea -cp ./libs/*;al-login-1.0.0.jar com.aionemu.loginserver.LoginServer
REM
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 1 goto error
goto end
:error
echo.
echo Login Server Terminated Abnormaly, Please Verify Your Files.
echo.
:end
echo.
echo Login Server Terminated.
echo.
pause