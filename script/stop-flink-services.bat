@echo off
echo.
echo ===============================================
echo       Flink微服务集群停止脚本
echo ===============================================
echo.

echo 正在停止所有相关进程...
echo.

REM 停止Java进程
taskkill /f /im java.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo 已停止Java进程
) else (
    echo 未找到运行的Java进程
)

REM 等待进程完全停止
timeout /t 3 >nul

echo.
echo ===============================================
echo       所有服务已停止
echo ===============================================
echo.
pause
