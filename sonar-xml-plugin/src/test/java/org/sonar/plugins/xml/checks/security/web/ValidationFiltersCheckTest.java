/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks.security.web;

import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class ValidationFiltersCheckTest {

  private static final SonarXmlCheck CHECK = new ValidationFiltersCheck();

  @Test
  void web_xml_without_filter() {
    SonarXmlCheckVerifier.verifyNoIssue("withoutFilters/web.xml", CHECK);
  }

  @Test
  void web_xml_incomplete_filter() {
    SonarXmlCheckVerifier.verifyIssues("incompleteFilters/web.xml", CHECK);
  }

  @Test
  void web_xml_incoherent_filters() {
    SonarXmlCheckVerifier.verifyIssues("incoherentFilters/web.xml", CHECK);
  }

  @Test
  void web_xml_with_filter() {
    SonarXmlCheckVerifier.verifyNoIssue("withFilters/web.xml", CHECK);
  }
}
