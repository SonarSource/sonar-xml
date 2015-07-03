/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.xml.parsers;

import org.xml.sax.SAXParseException;

/**
 * Exception for a parse error from which the parser cannot recover.
 */
class UnrecoverableParseError extends RuntimeException {

  static final String FAILUREMESSAGE = "The reference to entity \"null\"";

  private static final long serialVersionUID = 1L;

  public UnrecoverableParseError(SAXParseException e) {
    super(e);
  }
}
