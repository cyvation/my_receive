@echo off

title=�������Ŀ�Ķ�Ӧ��v2.1

mshta vbscript:msgbox("ע�ⴰ�����ã�����->ѡ��->ȡ�������ٱ༭ģʽ������ֹ������ͣ����",1,"��ʾ")(window.close)

title=�������Ŀ�Ķ�Ӧ��v2.1
color 0a

set install_path=%~dp0
cd /D %install_path%

rem �رն˿�ռ��--
for /f "tokens=5" %%i in ('netstat -aon ^| findstr ":5173"') do (
set n=%%i
)
if defined n (taskkill /f /pid %n%)
rem �رն˿�ռ��--
 
 
java  -Dfile.encoding=GBK  -jar  my_receive-2.1.jar

@pause

