/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;

/**
 * Repository for XML Parser messages.
 * 
 * @author Matthijs Galesloot
 * @since 1.0
 */
public final class XmlMessagesMatcher {

  private final List<AbstractMessagesRepository> messageRepositories = new ArrayList<AbstractMessagesRepository>();

  public XmlMessagesMatcher() {
    messageRepositories.add(new XmlMessagesRepository());
    messageRepositories.add(new XmlSchemaMessagesRepository());
  }

  public void setRuleForViolation(RuleFinder ruleFinder, Violation violation) {
    for (AbstractMessagesRepository repo : messageRepositories) {
      for (Entry<String, Pattern> entry : repo.getMessagePatterns().entrySet()) {

        if (entry.getValue().matcher(violation.getMessage()).lookingAt()) {
          Rule rule = ruleFinder.findByKey(repo.getKey(), entry.getKey());
          if (rule != null) {
            violation.setRule(rule);
            return;
          }
        }
      }
    }
  }
}