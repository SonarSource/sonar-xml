/*
 * SonarQube XML Plugin
 * Copyright (C) 2013-2025 SonarSource Sàrl
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
package com.sonar.it.xml;

import com.sonar.orchestrator.config.Configuration;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.Location;
import com.sonar.orchestrator.locator.Locators;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequiredForLanguagesTest {

  private static final Locators ORCHESTRATOR_LOCATORS = new Locators(Configuration.createEnv());
  private static final Location PLUGIN_LOCATION = FileLocation.byWildcardMavenFilename(
    new File("../../sonar-xml-plugin/target"), "sonar-xml-plugin-*.jar");

  @Test
  void test_required_for_languages() {
    File jarFile = ORCHESTRATOR_LOCATORS.locate(PLUGIN_LOCATION);
    Manifest manifest;
    try (JarFile jar = new JarFile(jarFile)) {
      manifest = jar.getManifest();
    } catch (IOException e) {
      throw new RuntimeException("Could not read manifest from plugin JAR", e);
    }
    String propertyValue = manifest.getMainAttributes().getValue("Plugin-RequiredForLanguages");
    assertThat(propertyValue).isEqualTo("xml");
  }

}
