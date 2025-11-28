@echo off
echo.
echo ===============================================
echo       Flink微服务集群启动脚本
echo ===============================================
echo.

REM 检查Java环境
java -version
if %errorlevel% neq 0 (
    echo 错误: 未检测到Java环境，请先安装JDK 8或更高版本
    pause
    exit /b 1
)

REM 检查Maven Daemon
where mvnd >nul 2>nul
if %errorlevel% neq 0 (
    echo 警告: 未检测到mvnd，将使用mvn命令
    set MVN_CMD=mvn
) else (
    set MVN_CMD=mvnd
)

echo 启动服务顺序:
echo 1. Nacos Server (http://localhost:8848)
echo 2. Gateway (http://localhost:8888)
echo 3. Flink V1 Service (http://localhost:8084)
echo 4. Flink V2 Service (http://localhost:8085)
echo 5. Flink Admin Service (http://localhost:8086)
echo.

pause

echo.
echo 正在启动服务...
echo.

REM 启动服务1: Nacos Server
echo ========================================
echo 正在启动 Nacos Server...
echo ========================================
start "Nacos Server" cmd /k "cd /d F:\Users\yzh\IdeaProjects\zt\yudao-boot-mini-zt && %MVN_CMD% spring-boot:run -Dspring-boot.run.profiles=nacos -pl yudao-flink-common"
timeout /t 5 >nul

REM 等待Nacos启动
echo 等待Nacos Server启动(10秒)...
timeout /t 10 >nul

REM 启动服务2: Gateway
echo ========================================
echo 正在启动 Gateway...
echo ========================================
start "Gateway" cmd /k "cd /d F:\Users\yzh\IdeaProjects\zt\yudao-boot-mini-zt && %MVN_CMD% spring-boot:run -Dspring-boot.run.profiles=local -pl yudao-cloud-gateway"
timeout /t 5 >nul

REM 启动服务3: Flink V1 Service
echo ========================================
echo 正在启动 Flink V1 Service...
echo ========================================
start "Flink V1 Service" cmd /k "cd /d F:\Users\yzh\IdeaProjects\zt\yudao-boot-mini-zt && %MVN_CMD% spring-boot:run -Dspring-boot.run.profiles=local -pl yudao-flink-v1-service"
timeout /t 5 >nul

REM 启动服务4: Flink V2 Service
echo ========================================
echo 正在启动 Flink V2 Service...
echo ========================================
start "Flink V2 Service" cmd /k "cd /d F:\Users\yzh\IdeaProjects\zt\yudao-boot-mini-zt && %MVN_CMD% spring-boot:run -Dspring-boot.run.profiles=local -pl yudao-flink-v2-service"
timeout /t 5 >nul

REM 启动服务5: Flink Admin Service
echo ========================================
echo 正在启动 Flink Admin Service...
echo ========================================
start "Flink Admin Service" cmd /k "cd /d F:\Users\yzh\IdeaProjects\zt\yudao-boot-mini-zt && %MVN_CMD% spring-boot:run -Dspring-boot.run.profiles=local -pl yudao-flink-admin"

echo.
echo ===============================================
echo       所有服务启动完成！
echo ===============================================
echo.
echo 服务访问地址:
echo  - Nacos Console: http://localhost:8848/nacos
echo  - API Gateway:   http://localhost:8888
echo  - Flink V1:      http://localhost:8084
echo  - Flink V2:      http://localhost:8085
echo  - Flink Admin:   http://localhost:8086
echo.
echo 注意事项:
echo  - 请确保Flink集群已启动并配置正确
echo  - 所有服务都已注册到Nacos
echo  - 使用Ctrl+C停止所有服务
echo.
pause
