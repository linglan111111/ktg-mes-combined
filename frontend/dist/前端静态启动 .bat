@echo off
title MES前端服务器
echo ================================
echo    MES系统前端服务器
echo ================================
echo.

REM 进入dist目录
cd /d D:\Projects\ktg-mes-ui\dist
set NODE_SKIP_PLATFORM_CHECK=1
REM 启动
echo 正在启动...
echo 访问地址: http://localhost:3000
echo 按 Ctrl+C 停止
echo.
node server.js