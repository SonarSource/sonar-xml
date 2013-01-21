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
package org.sonar.plugins.xml.language;

import org.sonar.channel.CodeReader;
import org.sonar.colorizer.HtmlCodeBuilder;
import org.sonar.colorizer.Tokenizer;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * This tokenizer will deal with start xml elements including
 * attributes and multiline stuff.
 * @author Julien HENRY
 *
 */
public class XmlStartElementTokenizer extends Tokenizer {

  private static final String ELEMENT_STARTED = "ELEMENT_STARTED";
  private static final String ATTRIBUTE_STARTED = "ATTRIBUTE_STARTED";
  private static final String ATTRIBUTE_VALUE_STARTED = "ATTRIBUTE_VALUE_STARTED";
  private static final String ELEMENT_TOKENIZER = "MULTILINE_ELEMENT_TOKENIZER";
  private final String tagBeforeElement;
  private final String tagAfterElement;
  private final String tagBeforeAttributeName;
  private final String tagAfterAttributeName;
  private final String tagBeforeAttributeValue;
  private final String tagAfterAttributeValue;

  public XmlStartElementTokenizer(String tagBeforeElement, String tagAfterElement,
      String tagBeforeAttributeName, String tagAfterAttributeName, String tagBeforeAttributeValue, String tagAfterAttributeValue) {
    this.tagBeforeElement = tagBeforeElement;
    this.tagAfterElement = tagAfterElement;
    this.tagBeforeAttributeName = tagBeforeAttributeName;
    this.tagAfterAttributeName = tagAfterAttributeName;
    this.tagBeforeAttributeValue = tagBeforeAttributeValue;
    this.tagAfterAttributeValue = tagAfterAttributeValue;
  }

  @Override
  public boolean consume(CodeReader code, HtmlCodeBuilder codeBuilder) {
    if (!isElementStarted(codeBuilder)) {
      if (code.peek() == '<') {
        codeBuilder.appendWithoutTransforming(tagBeforeElement);
        // Consume element start
        code.popTo(Pattern.compile("<\\??[:\\w][:\\-\\.\\w]*").matcher(""), codeBuilder);
        codeBuilder.appendWithoutTransforming(tagAfterElement);
        setElementStarted(codeBuilder, true);
        return true;
      }
      return false;
    }
    else {
      // Element already started
      return consumeStartElement(code, codeBuilder);
    }
  }

  private boolean consumeStartElement(CodeReader code, HtmlCodeBuilder codeBuilder) {
    // Consume every blanks
    if (code.popTo(Pattern.compile("\\s").matcher(""), codeBuilder) != -1) {
      return true;
    }
    else if (!isAttributeStarted(codeBuilder)) {
      if (consumeEndOfTagElement(code, codeBuilder)) {
        return true;
      }
      // Attribute start
      codeBuilder.appendWithoutTransforming(tagBeforeAttributeName);
      setAttributeStarted(codeBuilder, true);
      // Consume attribute name
      code.popTo(Pattern.compile("[^\\s=]*").matcher(""), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfterAttributeName);
      return true;
    } else {
      // attribute started
      if (!isAttributeValueStarted(codeBuilder)) {
        if (code.peek() == '=') {
          // Consume '='
          code.pop(codeBuilder);
          return true;
        }
        else if (code.peek() == '\'' || code.peek() == '"') {
          char quote = (char) code.peek();
          setAttributeValueStarted(codeBuilder, quote);
          codeBuilder.appendWithoutTransforming(tagBeforeAttributeValue);
          // consume the opening quote
          code.pop(codeBuilder);
          consumeAttributeValueUntilQuoteOrEol(code, codeBuilder, quote);
          return true;
        }
        return false;
      }
      else {
        // attribute value started
        char quote = getAttributeQuote(codeBuilder);
        codeBuilder.appendWithoutTransforming(tagBeforeAttributeValue);
        consumeAttributeValueUntilQuoteOrEol(code, codeBuilder, quote);
        return true;
      }
    }
  }

  private boolean consumeEndOfTagElement(CodeReader code, HtmlCodeBuilder codeBuilder) {
    // Check for empty element end like <br />
    return consumeEndElement(code, codeBuilder, "/>")
      || consumeEndElement(code, codeBuilder, "?>")
      || consumeEndElement(code, codeBuilder, ">");
  }

  private boolean consumeEndElement(CodeReader code, HtmlCodeBuilder codeBuilder, String endElement) {
    if (Arrays.equals(code.peek(endElement.length()), endElement.toCharArray())) {
      codeBuilder.appendWithoutTransforming(tagBeforeElement);
      // Consume end element
      for (int i = 0; i < endElement.length(); i++) {
        code.pop(codeBuilder);
      }
      codeBuilder.appendWithoutTransforming(tagAfterElement);
      setElementStarted(codeBuilder, false);
      return true;
    }
    return false;
  }

  /**
   * Consume attribute value until eol or same quote
   */
  private void consumeAttributeValueUntilQuoteOrEol(CodeReader code, HtmlCodeBuilder codeBuilder, char quote) {
    // consume attribute value until eol or same quote
    while (code.peek() != quote && code.peek() != '\r' && code.peek() != '\n') {
      code.pop(codeBuilder);
    }
    if (code.charAt(0) == quote) {
      // consume the closing quote
      code.pop(codeBuilder);
      setAttributeStarted(codeBuilder, false);
      setAttributeValueStarted(codeBuilder, null);
    }
    codeBuilder.appendWithoutTransforming(tagAfterAttributeValue);
  }

  private boolean isElementStarted(HtmlCodeBuilder codeBuilder) {
    Boolean b = (Boolean) codeBuilder.getVariable(ELEMENT_STARTED, Boolean.FALSE);
    return (b == Boolean.TRUE) && (this.equals(codeBuilder.getVariable(ELEMENT_TOKENIZER)));
  }

  private boolean isAttributeStarted(HtmlCodeBuilder codeBuilder) {
    Boolean b = (Boolean) codeBuilder.getVariable(ATTRIBUTE_STARTED, Boolean.FALSE);
    return (b == Boolean.TRUE) && (this.equals(codeBuilder.getVariable(ELEMENT_TOKENIZER)));
  }

  private boolean isAttributeValueStarted(HtmlCodeBuilder codeBuilder) {
    Character quote = (Character) codeBuilder.getVariable(ATTRIBUTE_VALUE_STARTED, null);
    return (quote != null) && (this.equals(codeBuilder.getVariable(ELEMENT_TOKENIZER)));
  }

  private Character getAttributeQuote(HtmlCodeBuilder codeBuilder) {
    return (Character) codeBuilder.getVariable(ATTRIBUTE_VALUE_STARTED, null);
  }

  private void setElementStarted(HtmlCodeBuilder codeBuilder, Boolean b) {
    codeBuilder.setVariable(ELEMENT_STARTED, b);
    codeBuilder.setVariable(ELEMENT_TOKENIZER, b ? this : null);
  }

  private void setAttributeStarted(HtmlCodeBuilder codeBuilder, Boolean b) {
    codeBuilder.setVariable(ATTRIBUTE_STARTED, b);
  }

  private void setAttributeValueStarted(HtmlCodeBuilder codeBuilder, Character quote) {
    codeBuilder.setVariable(ATTRIBUTE_VALUE_STARTED, quote);
  }

}
