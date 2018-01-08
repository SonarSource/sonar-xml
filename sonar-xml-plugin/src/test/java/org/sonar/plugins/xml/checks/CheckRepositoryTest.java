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
package org.sonar.plugins.xml.checks;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class CheckRepositoryTest {

  @Test
  public void properties() {
    assertThat(CheckRepository.REPOSITORY_KEY).isEqualTo("xml");
    assertThat(CheckRepository.REPOSITORY_NAME).isEqualTo("SonarAnalyzer");
    assertThat(CheckRepository.SONAR_WAY_PROFILE_NAME).isEqualTo("Sonar way");
  }

  @Test
  public void getCheckClasses() {
    assertThat(CheckRepository.getCheckClasses().size()).isEqualTo(CheckRepository.getChecks().size());
  }

  /**
   * Enforces that each check declared in list.
   */
  @Test
  public void getChecksEachCheckShouldBeDeclared() {
    int count = 0;
    List<File> files = (List<File>) FileUtils.listFiles(new File("src/main/java/org/sonar/plugins/xml/checks/"), new String[] {"java"}, false);
    for (File file : files) {
      if (file.getName().endsWith("Check.java") && !file.getName().endsWith("AbstractXmlCheck.java")) {
        count++;
      }
    }
    assertThat(CheckRepository.getChecks().size()).isEqualTo(count);
  }

  /**
   * Enforces that each check has test
   */
  @Test
  public void test() {
    List<Class> checks = CheckRepository.getCheckClasses();

    for (Class cls : checks) {
      if (!cls.getSimpleName().equals(ParsingErrorCheck.class.getSimpleName())) {
        String testName = '/' + cls.getName().replace('.', '/') + "Test.class";
        assertThat(getClass().getResource(testName))
          .overridingErrorMessage("No test for " + cls.getSimpleName())
          .isNotNull();
      }
    }
  }
}
