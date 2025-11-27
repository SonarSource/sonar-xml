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

import com.google.common.io.Files;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonarsource.sonarlint.core.analysis.AnalysisEngine;
import org.sonarsource.sonarlint.core.analysis.api.ActiveRule;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisEngineConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.analysis.api.ClientModuleFileSystem;
import org.sonarsource.sonarlint.core.analysis.api.ClientModuleInfo;
import org.sonarsource.sonarlint.core.analysis.api.Issue;
import org.sonarsource.sonarlint.core.analysis.command.AnalyzeCommand;
import org.sonarsource.sonarlint.core.analysis.command.RegisterModuleCommand;
import org.sonarsource.sonarlint.core.commons.api.SonarLanguage;
import org.sonarsource.sonarlint.core.commons.log.LogOutput;
import org.sonarsource.sonarlint.core.commons.log.SonarLintLogger;
import org.sonarsource.sonarlint.core.commons.progress.ProgressMonitor;
import org.sonarsource.sonarlint.core.plugin.commons.PluginsLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SonarLintTest {

  private static final String MODULE_KEY = "myModule";
  private static final LogOutput NOOP_LOG_OUTPUT = new LogOutput() {

    @Override
    public void log(@Nullable String formattedMessage, @NotNull Level level, @Nullable String stacktrace) {
      /* Don't pollute logs */
    }
  };
  private final ProgressMonitor progressMonitor = new ProgressMonitor(null);

  @TempDir
  public static File temp;

  private static AnalysisEngine sonarlintEngine;
  private static File baseDir;

  @BeforeAll
  static void prepare() {
    // 1. Configure the Engine environment
    AnalysisEngineConfiguration config = AnalysisEngineConfiguration.builder()
      .setWorkDir(temp.toPath())
      .build();

    SonarLintLogger.setTarget(NOOP_LOG_OUTPUT);

    // 2. Locate and Load the Plugin
    FileLocation xmlPlugin = FileLocation.byWildcardMavenFilename(new File("../../sonar-xml-plugin/target"), "sonar-xml-plugin-*.jar");
    var pluginJarLocation = Set.of(xmlPlugin.getFile().toPath());

    // Note: Ensure SonarLanguage.XML exists in your dependencies, otherwise use string "XML"
    var enabledLanguages = Set.of(SonarLanguage.XML);

    var pluginConfiguration = new PluginsLoader.Configuration(pluginJarLocation, enabledLanguages, false, Optional.empty());
    var loadedPlugins = new PluginsLoader().load(pluginConfiguration, Set.of()).getLoadedPlugins();

    // 3. Start the Engine
    sonarlintEngine = new AnalysisEngine(config, loadedPlugins, NOOP_LOG_OUTPUT);
    baseDir = temp;
  }

  @Test
  void simpleXml() throws Exception {
    // Prepare input file
    ClientInputFile inputFile = prepareInputFile();

    final List<Issue> issues = new ArrayList<>();

    // 4. Configure Analysis
    // Note: We explicitly activate the rule here because the new low-level API
    // doesn't automatically load the "Sonar Way" profile by default in this test context.
    AnalysisConfiguration configuration = AnalysisConfiguration.builder()
      .setBaseDir(baseDir.toPath())
      .addInputFile(inputFile)
      .addActiveRules(new ActiveRule("xml:S1778", SonarLanguage.XML.getPluginKey()))
      .build();

    // 5. Register Module (Required in new API)
    ClientModuleFileSystem clientFileSystem = getClientModuleFileSystem(inputFile);
    sonarlintEngine.post(new RegisterModuleCommand(new ClientModuleInfo(MODULE_KEY, clientFileSystem)), progressMonitor).get();

    // 6. Execute Analysis
    var command = new AnalyzeCommand(MODULE_KEY, configuration, issues::add, NOOP_LOG_OUTPUT);
    sonarlintEngine.post(command, progressMonitor).get();

    // 7. Assertions
    // Note: The new API 'Issue' object might treat severity differently (Impacts),
    // so we assert on RuleKey, Line, and Path as seen in the Java reference.
    System.out.println(issues);
    assertThat(issues)
      .extracting("ruleKey", "startLine", "inputFile", "overriddenImpacts")
      .containsOnly(
        tuple("xml:S1778", 2, inputFile, Map.of()));
  }

  private ClientInputFile prepareInputFile() throws IOException {
    final File file = new File(baseDir, "foo.xml");
    FileUtils.write(file, """
      <!-- Ohlala, there is a comment before prolog! -->
      <?xml version="1.0" encoding="UTF-8"?>
      <foo>
        <bar value='boom' />
      </foo>
      """, StandardCharsets.UTF_8);
    return createInputFile(file.toPath());
  }

  private ClientInputFile createInputFile(final Path path) {
    return new ClientInputFile() {

      @Override
      public String getPath() {
        return path.toString();
      }

      @Override
      public String relativePath() {
        return baseDir.toPath().relativize(path).toString();
      }

      @Override
      public URI uri() {
        return path.toUri();
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
        return Files.asCharSource(path.toFile(), StandardCharsets.UTF_8).read();
      }
    };
  }

  private static ClientModuleFileSystem getClientModuleFileSystem(ClientInputFile inputFile) {
    return new ClientModuleFileSystem() {
      @Override
      public Stream<ClientInputFile> files(@NotNull String s, @NotNull InputFile.Type type) {
        return Stream.of(inputFile);
      }

      @Override
      public Stream<ClientInputFile> files() {
        return Stream.of(inputFile);
      }
    };
  }

}
