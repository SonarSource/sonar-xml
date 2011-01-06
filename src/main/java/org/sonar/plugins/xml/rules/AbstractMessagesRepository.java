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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.utils.SonarException;
import org.sonar.check.Cardinality;

/**
 * Repository for XML validation messages.
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
class AbstractMessagesRepository extends RuleRepository {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractMessagesRepository.class);

  private Properties messages;

  public AbstractMessagesRepository(String repositoryKey, String languageKey) {
    super(repositoryKey, languageKey);
  }

  @Override
  public List<Rule> createRules() {

    List<Rule> rules = new ArrayList<Rule>();

    for (Entry entry : messages.entrySet()) {

      Rule rule = Rule.create(getKey(), (String) entry.getKey(), (String) entry.getKey());
      rule.setDescription((String) entry.getValue());
      rule.setPriority(RulePriority.CRITICAL);
      rule.setCardinality(Cardinality.SINGLE);
      rules.add(rule);
    }

    return rules;
  }

  public Properties getMessages() {
    return messages;
  }

  protected Properties loadMessages(String fileName) {
    InputStream input = AbstractMessagesRepository.class.getClassLoader().getResourceAsStream("org/apache/xerces/impl/msg/" + fileName);
    try {
      Properties properties = new Properties();
      properties.load(input);
      return properties;
    } catch (IOException e) {
      throw new SonarException(e);
    }
  }

  protected void setMessages(Properties messages) {
    this.messages = messages;
  }

  private Map<String, Pattern> patterns;

  public Map<String, Pattern> getMessagePatterns() {
    if (patterns == null) {
      patterns = new HashMap<String, Pattern>();

      String[] replacements = new String[] { ".*", ".*", ".*", ".*", ".*" };

      for (Entry entry : messages.entrySet()) {
        String regExp = (String) entry.getValue();

        // replace single quoted 1-4 markers
        regExp = StringUtils.replaceEach(regExp,
            new String[] { "'{0}'", "'{1}'", "'{2}'", "'{3}'", "'{4}'" }, replacements);
        // replace double quoted 1-4 markers
        regExp = StringUtils.replaceEach(regExp,
            new String[] { "\"{0}\"", "\"{1}\"", "\"{2}\"", "\"{3}\"", "\"{4}\"" }, replacements);
        // replace unquoted 1-4 markers
        regExp = StringUtils.replaceEach(regExp,
            new String[] { "{0}", "{1}", "{2}", "{3}", "{4}" }, replacements);

        // replace remaining regexp special characters
        regExp = StringUtils.replaceEach(regExp,
            new String[] { "?", "[", "]", "{", "}", "(", ")", "\"</{0}>\"", "''" },
            new String[] { ".", ".", ".", ".", ".", ".", ".", ".*", ".*" });

        try {
          Pattern pattern = Pattern.compile(regExp);
          patterns.put((String) entry.getKey(), pattern);
        } catch (PatternSyntaxException e) {
          LOG.debug("", e);
          // ignore
        }
      }
    }
    return patterns;
  }

}