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

import java.io.Closeable;
import java.io.IOException;
import org.junit.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UtilsTest {

  @Test
  public void closeQuietly_closes_stream() throws Exception {
    Closeable closeable = mock(Closeable.class);

    Utils.closeQuietly(closeable);

    verify(closeable).close();
  }

  @Test
  public void closeQuietly_ignores_errors() throws Exception {
    Closeable closeable = mock(Closeable.class);
    doThrow(new IOException("foo")).when(closeable).close();

    // no error
    Utils.closeQuietly(closeable);

    verify(closeable).close();
  }

  @Test
  public void closeQuietly_ignores_null_parameter() {
    // no error
    Utils.closeQuietly(null);
  }
}
