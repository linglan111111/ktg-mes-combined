@echo off
echo Java编译工具 - 自动处理进 程冲突
echo.

echo 1. 检查当前状态...
tasklist | findstr "java.exe" >nul
if errorlevel 1 (
    echo 无Java进程，直接编译...
    goto compile
)

echo 2. 分析现有Java进程...
for /f "tokens=2" %%p in ('tasklist ^| findstr "java.exe"') do (
    for /f "delims=" %%c in ('wmic process where processid^=%%p get commandline /value 2^>nul') do (
        for /f "tokens=2 delims==" %%v in ("%%c") do (
            set "cmd=%%v"
        )
    )
    
    REM 如果是项目进程，询问是否杀死
    echo !cmd! | findstr /i "D:\\Projects\\ktg-mes" >nul
    if !errorlevel!==0 (
        echo 发现项目进程 PID: %%p
        echo 命令行: !cmd!
        set /p kill="编译前需要杀死此进程，是否继续? (y/n): "
        if /i "!kill!"=="y" (
            echo 杀死进程 PID: %%p
            taskkill /f /pid %%p >nul
        ) else (
            echo 跳过编译
            pause
            exit /b
        )
    )
)

:compile
echo 3. 开始编译...
mvn clean install -DskipTests  
echo 编译完成！
echo.

echo 4. 编译后状态...
tasklist | findstr "java.exe" >nul && (
    echo 编译后有Java进程运行
) || (
    echo 编译后无Java进程
)
pause