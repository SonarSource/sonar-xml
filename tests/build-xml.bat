
rem run this script from webscanner folder

set SONAR_HOME=C:\bin\sonar-2.5-RC1
set SONAR_FLAGS=-Dsonar.host.url=http://localhost:9000 -Dsonar.jdbc.url=jdbc:postgresql://localhost/sonar -Dsonar.jdbc.driver=org.postgresql.Driver
set DEBUG=-X 

call mvn install  -Dmaven.test.skip
call xcopy /Y target\*.jar %SONAR_HOME%\extensions\plugins
start "Sonar Server" /MIN %SONAR_HOME%\bin\windows-x86-32\StartSonar.bat 

set mvncommand=mvn sonar:sonar

:mvn
rem 'ping' in order to wait a few seconds
ping 127.0.0.1 -n 10 -w 1000 > nul
rem try mvn sonar
call %mvncommand% -f source-its/projects/xhtml/pom.xml %SONAR_FLAGS% %DEBUG% > sonar-xml.log
rem check if sonar was available
find "[INFO] Sonar server can not be reached" *.log
rem previous command will set errorlevel to 0 if the log contained "sonar can not be reached"
IF %ERRORLEVEL% == 0 GOTO mvn

echo Error Level  %ERRORLEVEL%