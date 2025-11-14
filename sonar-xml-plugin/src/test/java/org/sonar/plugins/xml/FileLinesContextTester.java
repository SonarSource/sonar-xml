/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.plugins.xml;

import java.util.Map;
import java.util.TreeMap;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;

public class FileLinesContextTester implements FileLinesContextFactory {

  // String inputFileKey -> String metricKey -> Integer line -> Object value
  private final Map<String, Map<String, Map<Integer, Object>>> storage = new TreeMap<>();

  @Override
  public FileLinesContext createFor(InputFile inputFile) {
    return new Metrics(inputFile.key());
  }

  public Map<String, Map<Integer, Object>> metrics(InputFile inputFile) {
    return storage.getOrDefault(inputFile.key(), Map.of());
  }

  private class Metrics implements FileLinesContext {

    // String metricKey -> Integer line -> Object value
    private final Map<String, Map<Integer, Object>> storageBuffer = new TreeMap<>();
    private final String inputFileKey;

    public Metrics(String inputFileKey) {
      this.inputFileKey = inputFileKey;
    }

    @Override
    public void setIntValue(String metricKey, int line, int value) {
      put(metricKey, line, value);
    }

    @Override
    public void setStringValue(String metricKey, int line, String value) {
      put(metricKey, line, value);
    }

    private void put(String metricKey, int line, Object value) {
      storageBuffer
        .computeIfAbsent(metricKey, k -> new TreeMap<>())
        .put(line, value);
    }

    @Override
    public void save() {
      storage
        .computeIfAbsent(inputFileKey, k -> new TreeMap<>())
        .putAll(storageBuffer);
      storageBuffer.clear();
    }

  }

}
