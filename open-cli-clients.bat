@echo off
setlocal
pushd %~dp0

set COUNT=%1
if "%COUNT%"=="" set COUNT=2

for /L %%i in (1,1,%COUNT%) do (
  start "" java -cp out client.ChatClientMain
)

popd
endlocal
