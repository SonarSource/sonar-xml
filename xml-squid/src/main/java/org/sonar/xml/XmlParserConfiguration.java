/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.xml;

import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.checkNotNull;

public class XmlParserConfiguration {

  private final Charset charset;

  private XmlParserConfiguration(Builder builder) {
    this.charset = builder.charset;
  }

  public Charset getCharset() {
    return charset;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Charset charset = null;

    private Builder() {
    }

    public Builder setCharset(Charset charset) {
      this.charset = charset;
      return this;
    }

    public Charset getCharset() {
      return charset;
    }

    public XmlParserConfiguration build() {
      checkNotNull(charset, "charset is mandatory and cannot be left null");
      return new XmlParserConfiguration(this);
    }

  }

}
