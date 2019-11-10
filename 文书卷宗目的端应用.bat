@echo off

title=文书卷宗目的端应用v2.1

color 0a

rem 获取当前目录路径

set install_path=%~dp0
cd /D %install_path%
 
java   -Dfile.encoding=GBK -jar E:\SJFH2\my_receive-2.1.jar

pause
exit
