@echo off
setlocal
set BASE=D:\wwwroot\houcloud\backend\paas
set EC=%BASE%\errorcode\target\classes
set OUT=%BASE%\commons\target\errorcode-gen
set GEN=%BASE%\commons\src\main\java\com\houjiang\commons\errorcode\tool\ErrorCodeDocGenerator.java

if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%" 2>nul

javac -encoding UTF-8 -cp "%EC%" -d "%OUT%" "%GEN%" > "%OUT%\compile.log" 2>&1
echo COMPILE_EXIT=%ERRORLEVEL% >> "%OUT%\compile.log"

java -Dfile.encoding=UTF-8 -cp "%OUT%;%EC%" com.houjiang.commons.errorcode.tool.ErrorCodeDocGenerator D:\wwwroot\houcloud > "%OUT%\run.log" 2>&1
echo RUN_EXIT=%ERRORLEVEL% >> "%OUT%\run.log"
endlocal
