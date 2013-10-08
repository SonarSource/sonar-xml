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

import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.language.Xml;

import java.util.List;

/**
 * Repository for XML rules.
 */
public final class XmlRulesRepository extends RuleRepository {

  private final AnnotationRuleParser annotationRuleParser;

  public XmlRulesRepository(AnnotationRuleParser annotationRuleParser) {
    super(CheckRepository.REPOSITORY_KEY, Xml.KEY);
    setName(CheckRepository.REPOSITORY_NAME);
    this.annotationRuleParser = annotationRuleParser;
  }

  @Override
  public List<Rule> createRules() {
    return annotationRuleParser.parse(CheckRepository.REPOSITORY_KEY, CheckRepository.getCheckClasses());
  }
}
