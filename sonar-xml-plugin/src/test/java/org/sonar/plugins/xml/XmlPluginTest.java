/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlPluginTest {

  @Test
  public void count_extensions_for_sonarqube_server_6_0() throws Exception {
    Plugin.Context context = setupContext(SonarRuntimeImpl.forSonarQube(Version.create(6, 0), SonarQubeSide.SERVER));
    assertThat(context.getExtensions()).as("Number of extensions for SQ 6.0").hasSize(6);
  }

  @Test
  public void count_extensions_for_sonarqube_server_6_2() throws Exception {
    Plugin.Context context = setupContext(SonarRuntimeImpl.forSonarQube(Version.create(6, 2), SonarQubeSide.SERVER));
    assertThat(context.getExtensions()).as("Number of extensions for SQ 6.2").hasSize(6);
  }

  private Plugin.Context setupContext(SonarRuntime runtime) {
    Plugin.Context context = new Plugin.Context(runtime);
    new XmlPlugin().define(context);
    return context;
  }

}

