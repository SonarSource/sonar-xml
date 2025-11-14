/*
 * SonarQube XML Plugin
 * Copyright (C) 2013-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonarsource.sonarlint.core.StandaloneSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngine;
import org.sonarsource.sonarlint.core.commons.IssueSeverity;
import org.sonarsource.sonarlint.core.commons.Language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SonarLintTest {

  @TempDir
  public static File temp;

  private static StandaloneSonarLintEngine sonarlintEngine;
  private static File baseDir;

  @BeforeAll
  static void prepare() {
    FileLocation xmlPlugin = FileLocation.byWildcardMavenFilename(new File("../../sonar-xml-plugin/target"), "sonar-xml-plugin-*.jar");
    StandaloneGlobalConfiguration config = StandaloneGlobalConfiguration.builder()
      .addPlugin(xmlPlugin.getFile().toPath())
      .setSonarLintUserHome(temp.toPath())
      .setLogOutput((msg, level) -> System.out.println(String.format("[%s] %s", level.name(), msg)))
      .addEnabledLanguage(Language.XML)
      .build();
    sonarlintEngine = new StandaloneSonarLintEngineImpl(config);
    baseDir = temp;
  }

  @Test
  void simpleXml() throws Exception {
    // Rule S1778 is part of SonarWay (characters before prolog)
    ClientInputFile inputFile = prepareInputFile("foo.xml", """
      <!-- Ohlala, there is a comment before prolog! -->
      <?xml version="1.0" encoding="UTF-8"?>
      <foo>
        <bar value='boom' />
      </foo>
      """);

    List<Issue> issues = new ArrayList<>();
    StandaloneAnalysisConfiguration configuration = StandaloneAnalysisConfiguration.builder()
      .setBaseDir(baseDir.toPath())
      .addInputFile(inputFile)
      .build();
    sonarlintEngine.analyze(configuration, issues::add, null, null);

    assertThat(issues)
      .extracting("ruleKey", "startLine", "inputFile.path", "severity")
      .containsOnly(tuple("xml:S1778", 2, inputFile.relativePath(), IssueSeverity.CRITICAL));
  }

  private ClientInputFile prepareInputFile(String relativePath, String content) throws IOException {
    final File file = new File(baseDir, relativePath);
    FileUtils.write(file, content, StandardCharsets.UTF_8);
    return createInputFile(file.toPath());
  }

  private ClientInputFile createInputFile(Path path) {
    // replace default implementation with only what we need
    return new ClientInputFile() {

      @Override
      public String getPath() {
        return path.toString();
      }

      @Override
      public boolean isTest() {
        return false;
      }

      @Override
      public Charset getCharset() {
        return StandardCharsets.UTF_8;
      }

      @Override
      public <G> G getClientObject() {
        return null;
      }

      @Override
      public InputStream inputStream() throws IOException {
        return new FileInputStream(path.toFile());
      }

      @Override
      public String contents() throws IOException {
        return FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
      }

      @Override
      public String relativePath() {
        return path.toString();
      }

      @Override
      public URI uri() {
        return path.toUri();
      }
    };
  }

  @AfterAll
  static void stop() {
    sonarlintEngine.stop();
  }

}
