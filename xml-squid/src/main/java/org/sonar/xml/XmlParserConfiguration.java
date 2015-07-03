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
