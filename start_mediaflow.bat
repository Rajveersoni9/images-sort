@echo off
title MediaFlow AI - Local Engine
echo ========================================================
echo       MediaFlow AI - Local Photo & Video Sorter
echo ========================================================
echo.
echo Starting local AI engine on http://127.0.0.1:8000 ...
start http://127.0.0.1:8000
python python_engine/server.py
pause
