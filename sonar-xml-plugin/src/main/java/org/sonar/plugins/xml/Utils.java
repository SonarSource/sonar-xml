/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.xml;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.xml.compat.CompatibleInputFile;

public class Utils {
  private Utils() {
    // utility class, forbidden constructor
  }

  public static List<String> readLines(CompatibleInputFile file) throws IOException {
    try (BufferedReader reader = newBufferedReader(file)) {
      return reader.lines().collect(Collectors.toList());
    }
  }

  private static BufferedReader newBufferedReader(CompatibleInputFile file) throws IOException {
    return new BufferedReader(new StringReader(file.contents()));
  }

  public static String contents(Path path, Charset charset) throws IOException {
    return new String(Files.readAllBytes(path), charset);
  }

  /**
   * Close and ignore exception on errors.
   * It's not recommended to use this method, or at least its implementation
   * should be improved in order to log the exception.
   */
  public static void closeQuietly(@Nullable Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }
}
