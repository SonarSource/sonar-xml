# Github projects:

* jboss-ejb3-tutorial: git@github.com:jbossejb3/jboss-ejb3-tutorial.git
* sonar-java: git@github.com:SonarSource/sonar-java.git
* spring-framework: git@github.com:spring-projects/spring-framework.git

# cleaning projects

From within each project folder, remove all the non-xml files:
```
find . -type f ! -name '*.xml' -delete
rm -rf .git
```

To count the number of xml files remaining:
```
find . -type f -name '*.xml' | wc -l
```

To list all the xml files:
```
find . -type f -name '*.xml'
```

