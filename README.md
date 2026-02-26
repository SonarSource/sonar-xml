Code Quality and Security for XML [![Build Status](https://github.com/SonarSource/sonar-xml/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/SonarSource/sonar-xml/actions/workflows/build.yml) [![Quality Gate Status](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.xml%3Axml&metric=alert_status)](https://next.sonarqube.com/sonarqube/dashboard?id=org.sonarsource.xml%3Axml) [![Coverage](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.xml%3Axml&metric=coverage)](https://next.sonarqube.com/sonarqube/dashboard?id=org.sonarsource.xml%3Axml)
==========

This SonarSource project is a code analyzer for XML files.

Project homepage:
https://redirect.sonarsource.com/plugins/xml.html

Issue tracking:
https://jira.sonarsource.com/browse/SONARXML/

### Updating licenses:
When dependencies change, update the committed license files using the `updateLicenses` profile:
```sh
mvn clean package -PupdateLicenses
```
This regenerates licenses in `sonar-xml-plugin/src/main/resources/licenses/` based on current project dependencies.

License
--------

Copyright 2010-2025 SonarSource.

SonarQube analyzers released after November 29, 2024, including patch fixes for prior versions, are published under the [Sonar Source-Available License Version 1 (SSALv1)](LICENSE.txt).

See individual files for details that specify the license applicable to each file.
Files subject to the SSALv1 will be noted in their headers.
