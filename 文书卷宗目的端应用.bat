@echo off

title=�������Ŀ�Ķ�Ӧ��v2.1

mode con cols=120
color 09


rem LANG=zh_CN
rem ��ȡ��ǰĿ¼·��

set install_path=%~dp0
cd /D %install_path%
 
java   -Dfile.encoding=GBK -jar E:\SJFH2\my_receive-2.1.jar

