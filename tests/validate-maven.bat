
rem run this script from xml folder

set SONAR_HOME=C:\bin\sonar-2.7
set SONAR_FLAGS=-Dsonar.host.url=http://localhost:9000
set DEBUG=-X 

set mvncommand=mvn -N sonar:sonar -Dmaven.test.skip -Dsonar.language=xml -Dsonar.xml.sourceDirectory=. -Dsonar.xml.fileFilter=**/pom.xml -Dsonar.xml.schemas=http://maven.apache.org/POM/4.0.0

call %mvncommand% -f ../csharp/pom.xml %SONAR_FLAGS% %DEBUG% > sonar-maven-xml.log

