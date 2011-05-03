rem run this script from xml folder
rem the script starts analysis of maven files in the csharp folder

set DEBUG=-X

set mvncommand=mvn sonar:sonar -Dmaven.test.skip=true -Dsonar.language=xml -Dsonar.xml.sourceDirectory=. -Dsonar.xml.includeFileFilter=pom.xml -Dsonar.xml.schemas=http://maven.apache.org/POM/4.0.0
 
call %mvncommand% -f ../csharp/pom.xml %SONAR_FLAGS% %DEBUG% 
