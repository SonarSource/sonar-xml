
rem run this script from xml folder

set SONAR_HOME=C:\bin\sonar-2.7
set SONAR_FLAGS=-Dsonar.host.url=http://localhost:9000
set DEBUG=

set mvncommand=mvn sonar:sonar -Dmaven.test.skip -Dsonar.language=xml -Dsonar.xml.sourceDirectory=. -Dsonar.xml.includeFileFilter=pom.xml -Dsonar.xml.schemas=http://maven.apache.org/POM/4.0.0

rem call %mvncommand% -f ../csharp/pom.xml %SONAR_FLAGS% %DEBUG% > sonar-maven-xml.log

set mvncommand=mvn sonar:sonar -Dmaven.test.skip 

call %mvncommand% -f ../webscanner/htmltest/validator.w3.org.xml %SONAR_FLAGS% %DEBUG%
