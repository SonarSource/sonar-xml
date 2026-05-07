/*
 * SonarQube XML Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.io.TempDir;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalyzeFilesAndTrackParams;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalyzeFilesResponse;
import org.sonarsource.sonarlint.core.rpc.protocol.client.issue.RaisedIssueDto;
import org.sonarsource.sonarlint.core.rpc.protocol.common.ClientFileDto;
import org.sonarsource.sonarlint.core.rpc.protocol.common.Language;
import org.sonarsource.sonarlint.core.rpc.protocol.common.TextRangeDto;
import org.sonarsource.sonarlint.core.test.utils.SonarLintBackendFixture;
import org.sonarsource.sonarlint.core.test.utils.SonarLintTestRpcServer;
import org.sonarsource.sonarlint.core.test.utils.junit5.SonarLintTest;
import org.sonarsource.sonarlint.core.test.utils.junit5.SonarLintTestHarness;
import org.sonarsource.sonarlint.core.test.utils.plugins.Plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

// Inspired by sonar-JS and sonar-cobol implementations.
public class SonarLintIntegrationTest {
  private static final String CONFIG_SCOPE_ID = "CONFIG_SCOPE_ID";

  private SonarLintBackendFixture.FakeSonarLintRpcClient client;
  private SonarLintTestRpcServer backend;

  private static final String FILE_NAME = "foo.xml";
  private static final String FILE_CONTENTS = """
      <!-- Ohlala, there is a comment before prolog! -->
      <?xml version="1.0" encoding="UTF-8"?>
      <foo>
        <bar value='boom' />
      </foo>
      """;

  @SonarLintTest
  void simpleXml(SonarLintTestHarness harness, @TempDir Path baseDir) throws IOException {
    ClientFileDto fileDto = createFile(baseDir, FILE_NAME, FILE_CONTENTS);
    initWithFiles(harness, baseDir, fileDto);

    List<RaisedIssueDto> issues = analyzeFileAndGetIssues(fileDto.getUri());

    assertThat(issues)
      .extracting(
        RaisedIssueDto::getRuleKey,
        RaisedIssueDto::getTextRange)
      .usingRecursiveFieldByFieldElementComparator()
      .containsOnly(tuple("xml:S1778", new TextRangeDto(2, 0, 2, 5)));
  }

  private List<RaisedIssueDto> analyzeFileAndGetIssues(URI fileUri) {
    UUID analysisId = UUID.randomUUID();
    AnalyzeFilesAndTrackParams params = new AnalyzeFilesAndTrackParams(CONFIG_SCOPE_ID, analysisId, List.of(fileUri), Map.of(), false);
    AnalyzeFilesResponse analysisResult =
      backend
        .getAnalysisService()
        .analyzeFilesAndTrack(params)
        .join();
    assertThat(analysisResult.getFailedAnalysisFiles()).isEmpty();
    await()
      .atMost(5, TimeUnit.SECONDS)
      .untilAsserted(() -> assertThat(client.getRaisedIssuesForScopeIdAsList(CONFIG_SCOPE_ID)).isNotEmpty());
    return client.getRaisedIssuesForScopeId(CONFIG_SCOPE_ID).get(fileUri);
  }

  private void initWithFiles(SonarLintTestHarness harness, Path baseDir, ClientFileDto fileDTOs) {
    FileLocation xmlPlugin = FileLocation.byWildcardMavenFilename(new File("../../sonar-xml-plugin/target"), "sonar-xml-plugin-*.jar");

    client = harness
      .newFakeClient()
      .withInitialFs(CONFIG_SCOPE_ID, baseDir, List.of(fileDTOs))
      .build();

    backend = harness
      .newBackend()
      .withStandaloneEmbeddedPluginAndEnabledLanguage(new Plugin(Set.of(Language.XML), xmlPlugin.getFile().toPath(), "", ""))
      .withUnboundConfigScope(CONFIG_SCOPE_ID)
      .start(client);
  }

  private static ClientFileDto createFile(Path folderPath, String fileName, String content) throws IOException {
    Path filePath = folderPath.resolve(fileName);
    Files.writeString(filePath, content);
    return new ClientFileDto(
        filePath.toUri(), folderPath.relativize(filePath), CONFIG_SCOPE_ID, false, null, filePath, null, Language.XML, true);
  }
}
