/*
 * Sonar XML Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.xml.checks;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.fest.assertions.Assertions.assertThat;

public class CheckRepositoryTest {

  @Test
  public void properties() {
    assertThat(CheckRepository.REPOSITORY_KEY).isEqualTo("xml");
    assertThat(CheckRepository.REPOSITORY_NAME).isEqualTo("Sonar");
    assertThat(CheckRepository.SONAR_WAY_PROFILE_NAME).isEqualTo("Sonar way");
  }

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

  @Test
  public void getCheckClasses() {
    assertThat(CheckRepository.getCheckClasses().size()).isEqualTo(CheckRepository.getChecks().size());
  }

  @Test
  public void everyCheckIsInternationalizedAndTested() {
    List<Class> checks = CheckRepository.getCheckClasses();

    for (Class cls : checks) {
      String testName = '/' + cls.getName().replace('.', '/') + "Test.class";
      assertThat(getClass().getResource(testName))
          .overridingErrorMessage("No test for " + cls.getSimpleName())
          .isNotNull();
    }

    ResourceBundle resourceBundle = ResourceBundle.getBundle("org.sonar.l10n.xml", Locale.ENGLISH);

    List<Rule> rules = new AnnotationRuleParser().parse("repositoryKey", checks);
    for (Rule rule : rules) {
      resourceBundle.getString("rule." + CheckRepository.REPOSITORY_KEY + "." + rule.getKey() + ".name");
      assertThat(getClass().getResource("/org/sonar/l10n/xml/rules/xml/" + rule.getKey() + ".html"))
          .overridingErrorMessage("No description for " + rule.getKey())
          .isNotNull();

      assertThat(rule.getDescription())
          .overridingErrorMessage("Description of " + rule.getKey() + " should be in separate file")
          .isNull();

      for (RuleParam param : rule.getParams()) {
        resourceBundle.getString("rule." + CheckRepository.REPOSITORY_KEY + "." + rule.getKey() + ".param." + param.getKey());

        assertThat(param.getDescription())
            .overridingErrorMessage("Description for param " + param.getKey() + " of " + rule.getKey() + " should be in separate file")
            .isEmpty();
      }
    }
  }

}
