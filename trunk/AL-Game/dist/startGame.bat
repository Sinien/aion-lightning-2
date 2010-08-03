@echo off
title Aion-Lightning GameServer Console

REM Start...
:start
echo Starting Aion-Lightning Game Server.
echo.

REM SET PATH="Type here your path to java jdk/jre (including bin folder)."
REM NOTE: Remove tag REM from previous line.

REM -------------------------------------
REM Default parameters for a basic server.
java -Xms512m -Xmx1536m -ea -cp ./libs/*;al-game-1.0.0.jar com.aionemu.gameserver.GameServer
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
if ERRORLEVEL 0 goto end

REM Restart...
:restart
echo.
echo Administrator Restart ...
echo.
goto start

REM Error...
:error
echo.
echo Server is terminated abnormaly ...
echo.
goto end

REM End...
:end
echo.
echo Server is terminated ...
echo.
pause
