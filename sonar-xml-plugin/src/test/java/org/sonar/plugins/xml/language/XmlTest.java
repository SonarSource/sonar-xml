/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.language;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.Settings;
import org.sonar.plugins.xml.XmlPlugin;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlTest {

  private Settings settings;
  private Xml xml;

  @Before
  public void setUp() {
    settings = new MapSettings();
    xml = new Xml(settings);
  }

  @Test
  public void defaultSuffixes() {
    settings.setProperty(XmlPlugin.FILE_SUFFIXES_KEY, "");
    assertThat(xml.getFileSuffixes()).containsOnly(".xml");
  }

  @Test
  public void customSuffixes() {
    settings.setProperty(XmlPlugin.FILE_SUFFIXES_KEY, ".xhtml");
    assertThat(xml.getFileSuffixes()).containsOnly(".xhtml");
  }

}
