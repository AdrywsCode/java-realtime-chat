@echo off
setlocal
pushd %~dp0

where rg >nul 2>nul
if %errorlevel%==0 (
  rg --files -g "*.java" > .sources.tmp
) else (
  dir /b /s *.java > .sources.tmp
)

if not exist out mkdir out
javac -encoding UTF-8 -d out @.sources.tmp
if %errorlevel% neq 0 (
  del .sources.tmp
  echo Compilation failed.
  popd
  endlocal
  exit /b 1
)
del .sources.tmp

start "" java -cp out server.ChatServer
start "" java -cp out client.gui.ChatWindow
start "" java -cp out client.gui.ChatWindow

popd
endlocal
