/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class XmlPluginTest {

  private XmlPlugin plugin;

  @Before
  public void setUp() {
    plugin = new XmlPlugin();
  }

  @Test
  public void test() {
    assertThat(plugin.getExtensions().size()).isGreaterThan(0);
  }
}
