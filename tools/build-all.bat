@echo off
@setlocal

set PATH=%CD%;%PATH%

set CLEAN=
set COMPILE=
set INSTALL=
set DEPLOY=
set ASSEMBLY_ASSEMBLY=
set ECLIPSE_CLEAN=
set ECLIPSE_ECLIPSE=
set ECLIPSE_M2ECLIPSE=

for %%A in (%*) do (
	if %%A == clean (
		set CLEAN=clean
	)else if %%A == compile (
		set COMPILE=compile
	)else if %%A == install (
		set INSTALL=install
	)else if %%A == deploy (
		set DEPLOY=deploy
	)else if %%A == assembly:assembly (
		set ASSEMBLY_ASSEMBLY=assembly:assembly
	)else if %%A == eclipse:clean (
		set ECLIPSE_CLEAN=eclipse:clean
	)else if %%A == eclipse:eclipse (
		set ECLIPSE_ECLIPSE=eclipse:eclipse
	)else if %%A == eclipse:m2eclipse (
		set ECLIPSE_M2ECLIPSE=eclipse:m2eclipse
	)else (
		echo.
		echo Invalid goal '%%A'!
		echo.
		goto :end
	)
)

if "%1" == "" (
	set CLEAN=clean
	set INSTALL=install
	set ASSEMBLY_ASSEMBLY=assembly:assembly
)


cd ..
call :execute-maven %ECLIPSE_CLEAN% %CLEAN%
call :execute-maven-inside al-commons %COMPILE% %INSTALL% %DEPLOY%
call :execute-maven-inside al-game %COMPILE% %ASSEMBLY_ASSEMBLY%
call :execute-maven-inside al-login %COMPILE% %ASSEMBLY_ASSEMBLY%
call :execute-maven-inside al-cserver %COMPILE% %ASSEMBLY_ASSEMBLY%
call :execute-maven %ECLIPSE_ECLIPSE% %ECLIPSE_M2ECLIPSE%
cd tools

echo.
echo Done:
for %%A in (%ECLIPSE_CLEAN% %CLEAN% %COMPILE% %INSTALL% %DEPLOY% %ASSEMBLY_ASSEMBLY% %ECLIPSE_ECLIPSE% %ECLIPSE_M2ECLIPSE%) do (
	echo  - %%A
)

echo This script is made by NB4L1, so a big thanks to him.
echo.
pause

:end
@endlocal
goto :eof


rem #############################################
:execute-maven
	if "%*" NEQ "" (
		call execute-maven.bat %*
	)
goto :eof

:execute-maven-inside
	for /f "tokens=1*" %%A in ("%*") do (
		cd %%A
		if "%%B" NEQ "" (
			call execute-maven.bat %%B
		)
		cd ..
	)
goto :eof
rem #############################################
