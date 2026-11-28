@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0sync_helper.ps1" %*
pause
