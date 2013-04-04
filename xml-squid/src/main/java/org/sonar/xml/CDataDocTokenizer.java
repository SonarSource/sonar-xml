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
package org.sonar.xml;

import org.sonar.channel.CodeReader;
import org.sonar.colorizer.HtmlCodeBuilder;
import org.sonar.colorizer.Tokenizer;

import java.util.Arrays;
import java.util.regex.Pattern;

public class CDataDocTokenizer extends Tokenizer {

  private static final String CDATA_START = "<![CDATA[";
  private static final String CDATA_END = "]]>";

  private static final String CDATA_STARTED = "CDATA_STARTED";
  private static final String CDATA_TOKENIZER = "MULTILINE_CDATA_TOKENIZER";
  private final char[] startToken;
  private final char[] endToken;
  private final String tagBefore;
  private final String tagAfter;

  public CDataDocTokenizer(String tagBefore, String tagAfter) {
    this.tagBefore = tagBefore;
    this.tagAfter = tagAfter;
    this.startToken = CDATA_START.toCharArray();
    this.endToken = CDATA_END.toCharArray();
  }

  public boolean isCDATABloc(CodeReader code, HtmlCodeBuilder codeBuilder) {
    return isCDATAStarted(codeBuilder)
      || (code.peek() != '\n'
        && code.peek() != '\r'
        && (code.peek() == startToken[0] && Arrays.equals(code.peek(startToken.length), startToken))
      );
  }

  @Override
  public boolean consume(CodeReader code, HtmlCodeBuilder codeBuilder) {
    if (!isCDATAStarted(codeBuilder) && code.peek() == startToken[0] && Arrays.equals(code.peek(startToken.length), startToken)) {
      codeBuilder.appendWithoutTransforming(tagBefore);
      // Consume CDATA start
      code.popTo(Pattern.compile(Pattern.quote(CDATA_START)).matcher(""), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      setCDATAStarted(codeBuilder, true);
      return true;
    }
    if (isCDATAStarted(codeBuilder)) {
      if (code.peek() == endToken[0] && Arrays.equals(code.peek(endToken.length), endToken)) {
        codeBuilder.appendWithoutTransforming(tagBefore);
        // Consume CDATA end
        code.popTo(Pattern.compile(Pattern.quote(CDATA_END)).matcher(""), codeBuilder);
        codeBuilder.appendWithoutTransforming(tagAfter);
        setCDATAStarted(codeBuilder, false);
      }
      else {
        // Consume everything between CDATA
        code.pop(codeBuilder);
      }
      return true;
    }

    return false;
  }

  private boolean isCDATAStarted(HtmlCodeBuilder codeBuilder) {
    Boolean b = (Boolean) codeBuilder.getVariable(CDATA_STARTED, Boolean.FALSE);
    return (b == Boolean.TRUE) && (this.equals(codeBuilder.getVariable(CDATA_TOKENIZER)));
  }

  private void setCDATAStarted(HtmlCodeBuilder codeBuilder, Boolean b) {
    codeBuilder.setVariable(CDATA_STARTED, b);
    codeBuilder.setVariable(CDATA_TOKENIZER, b ? this : null);
  }

}
