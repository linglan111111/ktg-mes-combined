@echo off
chcp 936 >nul
echo ========================================
echo         日常同步脚本
echo         只同步变化的文件
echo ========================================
echo.

cd /d D:\Projects\ktg-mes-combined

:: 1. 同步后端（只同步变化的文件）
echo [1/2] 同步后端...
robocopy "D:\Projects\ktg-mes" "backend" /MIR /XO /FFT /XA:SH /XD .git node_modules target /XF .gitignore /R:1 /W:1 /NP /LOG:sync.log

:: 2. 同步前端（只同步变化的文件）
echo [2/2] 同步前端...
robocopy "D:\Projects\ktg-mes-ui" "frontend" /MIR /XO /FFT /XA:SH /XD .git node_modules /XF .gitignore /R:1 /W:1 /NP /LOG+:sync.log

echo.
echo 同步完成！
echo 查看变化的文件: git status
echo 查看详细日志: sync.log
echo.
pause