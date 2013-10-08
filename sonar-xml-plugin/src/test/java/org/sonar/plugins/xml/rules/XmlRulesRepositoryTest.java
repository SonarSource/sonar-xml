/*
 * SonarQube XML Plugin
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
package org.sonar.plugins.xml.rules;

import org.junit.Test;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.plugins.xml.checks.CheckRepository;

import static org.fest.assertions.Assertions.assertThat;

public class XmlRulesRepositoryTest {

  @Test
  public void constructor() {
    XmlRulesRepository repo = new XmlRulesRepository(mockAnnotationRuleParser());
    assertThat(repo.getKey()).isEqualTo(CheckRepository.REPOSITORY_KEY);
    assertThat(repo.getName()).isEqualTo(CheckRepository.REPOSITORY_NAME);
  }

  @Test
  public void createRules() {
    XmlRulesRepository repo = new XmlRulesRepository(mockAnnotationRuleParser());
    assertThat(repo.createRules().size()).isEqualTo(CheckRepository.getChecks().size());
  }

  private AnnotationRuleParser mockAnnotationRuleParser() {
    return new AnnotationRuleParser();
  }

}
