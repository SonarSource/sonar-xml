/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.checks;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CheckRepository {

  public static final String REPOSITORY_KEY = "xml";
  public static final String REPOSITORY_NAME = "SonarQube";
  public static final String SONAR_WAY_PROFILE_NAME = "Sonar way";

  private CheckRepository() {
  }

  public static List<AbstractXmlCheck> getChecks() {
    return Arrays.asList(
      new IllegalTabCheck(),
      new IndentCheck(),
      new NewlineCheck(),
      new XmlSchemaCheck(),
      new CharBeforePrologCheck(),
      new XPathCheck());
  }

  public static List<Class> getCheckClasses() {
    return getChecks().stream().map(AbstractXmlCheck::getClass).collect(Collectors.toList());
  }

}
