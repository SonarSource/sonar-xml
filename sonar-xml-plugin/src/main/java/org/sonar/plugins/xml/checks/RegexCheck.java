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
package org.sonar.plugins.xml.checks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.SonarException;
import org.sonar.check.Cardinality;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

/**
 * Check against regular expressions.
 *
 * @author Julien Gaston
 */
@Rule(key = "RegexCheck", priority = Priority.MAJOR, cardinality = Cardinality.MULTIPLE)
public class RegexCheck extends AbstractXmlCheck {

  private static final Logger LOG = LoggerFactory.getLogger(RegexCheck.class);

  @RuleProperty(key = "regex", type = "TEXT")
  private String regex;

  @RuleProperty(key = "filePattern")
  private String filePattern;

  @RuleProperty(key = "message")
  private String message;

  public String getRegex() {
    return regex;
  }

  public String getMessage() {
    return message;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setFilePattern(String pattern) {
    this.filePattern = pattern;
  }

  @Override
  public void validate(XmlSourceCode xmlSourceCode) {
    setWebSourceCode(xmlSourceCode);

    if (regex != null) {
      try {
        Pattern pattern = Pattern.compile(regex);
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                xmlSourceCode.createInputStream()));
        int lineNb = 1;
        String line;
        while ((line = reader.readLine()) != null) {
          Matcher matcher = pattern.matcher(line);
          if (matcher.find()) {
            createViolation(lineNb, "Invalid pattern");
          }
          ++lineNb;
        }

        reader.close();
      } catch (PatternSyntaxException e) {
        throw new SonarException(e);
      } catch (IOException e) {
        LOG.warn("Unable to analyse file {}", xmlSourceCode.toString(), e);
      }
    }
  }
}
