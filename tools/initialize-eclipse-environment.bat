@echo off

call build-all.bat eclipse:clean clean install eclipse:m2eclipse

echo.
echo Eclipse environment initialized.
echo.