/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.plugins.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;

class XmlTest {

  private MapSettings settings;
  private Xml xml;

  @BeforeEach
  void setUp() {
    settings = new MapSettings();
    xml = new Xml(settings.asConfig());
  }

  @Test
  void defaultSuffixes() {
    settings.setProperty(XmlPlugin.FILE_SUFFIXES_KEY, "");
    assertThat(xml.getFileSuffixes()).containsOnly(".xml");
  }

  @Test
  void customSuffixes() {
    settings.setProperty(XmlPlugin.FILE_SUFFIXES_KEY, ".xhtml");
    assertThat(xml.getFileSuffixes()).containsOnly(".xhtml");
  }

}
