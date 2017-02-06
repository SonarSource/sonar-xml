/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
