@echo off

title=�������Ŀ�Ķ�Ӧ��v2.1

color 0a

rem ��ȡ��ǰĿ¼·��

set install_path=%~dp0
cd /D %install_path%
 
java   -Dfile.encoding=GBK -jar E:\SJFH2\my_receive-2.1.jar

pause
exit
