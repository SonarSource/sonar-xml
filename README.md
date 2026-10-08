<!-- Sonar Marketing hosts these approved brand assets on its Kentico Kontent CDN (assets-eu-01.kc-usercontent.com). Shared URLs are intentional; consult Marketing before replacing them. -->
<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://assets-eu-01.kc-usercontent.com/ef593040-b591-0198-9506-ed88b30bc023/a23fc7ba-23f0-489a-829d-ed88c0748521/Sonar_Logo_Dark%20Backgrounds.svg">
    <img src="https://assets-eu-01.kc-usercontent.com/ef593040-b591-0198-9506-ed88b30bc023/82c13eba-d95c-4bb8-8007-7ce77c14e043/Sonar_Logo_Light%20Backgrounds.svg" alt="Sonar logo" width="400">
  </picture>
</p>

[![Build Status](https://github.com/SonarSource/sonar-xml/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/SonarSource/sonar-xml/actions/workflows/build.yml) [![Quality Gate Status](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.xml%3Axml&metric=alert_status)](https://next.sonarqube.com/sonarqube/dashboard?id=org.sonarsource.xml%3Axml) [![Coverage](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.xml%3Axml&metric=coverage)](https://next.sonarqube.com/sonarqube/dashboard?id=org.sonarsource.xml%3Axml)

<!-- sonar-marketing:start -->
<!-- Marketing maintains this section. For wording changes, consult the relevant Product Marketing Manager (PMM). Repository CODEOWNERS review accuracy and merge changes. -->

# Code quality and security for XML

This repository contains the SonarQube static analyzer for XML files. It includes the analyzer implementation and checks for developers maintaining XML analysis.

To learn more about the SonarQube product family, visit the [Sonar website](https://www.sonarsource.com/products/sonarqube/).

<!-- sonar-marketing:end -->

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
