@echo off
@setlocal

rem #############################################
rem # Configure this, if you don't have 'svn' or 'mvn' in the path!
set PATH=%PATH%;C:\Program Files\Subversion\bin

echo.
set CHECK_PATH_TMP=%PATH%
:checkPath
for /f "tokens=1* delims=;" %%A in ("%CHECK_PATH_TMP%") do (
	if exist %%A\mvn.bat set MAVEN_EXISTS=1
	if exist %%A\mvn.exe set MAVEN_EXISTS=1
	if exist %%A\svn.bat set SVN_EXISTS=1
	if exist %%A\svn.exe set SVN_EXISTS=1
	set CHECK_PATH_TMP=%%B
	goto :checkPath
)

if not defined MAVEN_EXISTS (
	echo.
	echo Could not find 'mvn' on the path!
	echo.
	goto :end
)

if not defined SVN_EXISTS (
	echo.
	echo Could not find 'svn' on the path!
	echo.
	goto :end
)

if not defined MAVEN_OPTS (
	set MAVEN_OPTS=-Xms64m -Xmx256m
)

rem # Toggle comments, if you will need sources and docs
set FLAGS=%FLAGS% -Dmaven.test.skip=true
rem # set FLAGS=%FLAGS% -DdownloadSources=true
rem # set FLAGS=%FLAGS% -DdownloadJavadocs=true
rem #############################################

call mvn %* %FLAGS%

if %ERRORLEVEL% NEQ 0 (
	echo.
	echo A problem appeared while executing goal '%*' with flags '%FLAGS%'!
	echo.
	pause
)

:end
@endlocal
goto :eof
