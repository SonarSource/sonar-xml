
rem run this script from xml folder

set SONAR_HOME=C:\bin\sonar-2.7
set SONAR_FLAGS=-Dsonar.host.url=http://localhost:9000
set DEBUG=-X 

call mvn install -Dmaven.test.skip
call xcopy /Y target\*.jar %SONAR_HOME%\extensions\plugins
start "Sonar Server" /MIN %SONAR_HOME%\bin\windows-x86-64\StartSonar.bat 

set mvncommand=mvn sonar:sonar

:mvn
rem 'ping' in order to wait a few seconds
ping 127.0.0.1 -n 10 -w 1000 > nul
rem try mvn sonar
call %mvncommand% -f source-its/projects/xhtml/pom.xml %SONAR_FLAGS% %DEBUG% > sonar-xml.log
rem check if sonar was available
find "Sonar server can not be reached" *.log
rem previous command will set errorlevel to 0 if the log contained "sonar server can not be reached"
IF %ERRORLEVEL% == 0 GOTO mvn

echo Error Level  %ERRORLEVEL%
