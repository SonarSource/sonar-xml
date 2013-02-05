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
package org.sonar.xml.api;

import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;

public enum XmlGrammar implements GrammarRuleKey {

  DOCUMENT,

  CHAR,

  WHITESPACE,
  NAME,
  ENTITY_VALUE,
  ATT_VALUE,
  SYSTEM_LITERAL,
  PUBID_LITERAL,

  COMMENT,

  PI,
  PI_TARGET,

  CHAR_REF,
  REFERENCE,
  ENTITY_REF,
  PE_REFERENCE, ;

  private static final String CHAR_REGEXP = "[\u0001-\uD7FF\uE000-\uFFFD]";

  private static final String WHITESPACE_REGEXP = "[ \t\r\n]++";
  private static final String NAME_START_CHAR_REGEXP = "[:A-Z_a-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D" +
    "\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]";
  private static final String NAME_CHAR_REGEXP = "(?:" + NAME_START_CHAR_REGEXP + "|[-.0-9\u00B7\u0300-\u036F\u203F-\u2040])";
  private static final String NAME_REGEXP = NAME_START_CHAR_REGEXP + NAME_CHAR_REGEXP + "*+";
  private static final String PUBID_CHAR_REGEXP = "[ \r\na-zA-Z0-9-'()+,./:=?;!*#@$_%]";

  private static final String COMMENT_REGEXP = "<!--" + "(?:(?!-)" + CHAR_REGEXP + "|-" + "(?!-)" + CHAR_REGEXP + ")*+" + "-->";

  private static final String CHAR_REF_REGEXP = "(?:" + "&#" + "(?:[0-9]++|x[0-9a-fA-F]++);" + ")";

  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    // b.rule(DOCUMENT).is(PROLOG, ELEMENT, b.zeroOrMore(MISC));

    b.rule(CHAR).is(b.regexp(CHAR_REGEXP));

    b.rule(WHITESPACE).is(b.skippedTrivia(b.regexp(WHITESPACE_REGEXP))).skip();
    b.rule(NAME).is(b.regexp(NAME_REGEXP));
    b.rule(ENTITY_VALUE).is(
        b.firstOf(
            b.sequence('"', b.zeroOrMore(b.firstOf(b.regexp("[^%&\"]++"), PE_REFERENCE, REFERENCE)), '"'),
            b.sequence('\'', b.zeroOrMore(b.firstOf(b.regexp("[^%&\']++"), PE_REFERENCE, REFERENCE)), '\'')));
    b.rule(ATT_VALUE).is(
        b.firstOf(
            b.sequence('"', b.zeroOrMore(b.firstOf(b.regexp("[^<&\"]++"), REFERENCE)), '"'),
            b.sequence('\'', b.zeroOrMore(b.firstOf(b.regexp("[^<&\']++"), REFERENCE)), '\'')));
    b.rule(SYSTEM_LITERAL).is(
        b.firstOf(
            b.sequence('"', b.regexp("[^\"]" + "*+"), '"'),
            b.sequence('\'', b.regexp("[^']" + "*+"), '\'')));
    b.rule(PUBID_LITERAL).is(
        b.firstOf(
            b.sequence('"', b.regexp(PUBID_CHAR_REGEXP + "*+"), '"'),
            b.sequence('\'', b.regexp("(?:(?!')" + PUBID_CHAR_REGEXP + ")*+"), '\'')));

    b.rule(COMMENT).is(b.commentTrivia(b.regexp(COMMENT_REGEXP))).skip();

    b.rule(PI).is("<?", PI_TARGET, b.optional(WHITESPACE, b.regexp("((?!\\?>)" + CHAR_REGEXP + ")++")), "?>");
    b.rule(PI_TARGET).is(
        b.nextNot(
            b.firstOf('x', 'X'),
            b.firstOf('m', 'M'),
            b.firstOf('l', 'L')),
        NAME);

    b.rule(CHAR_REF).is(b.regexp(CHAR_REF_REGEXP));
    b.rule(REFERENCE).is(b.firstOf(ENTITY_REF, CHAR_REF));
    b.rule(ENTITY_REF).is('&', NAME, ';');
    b.rule(PE_REFERENCE).is('%', NAME, ';');

    b.setRootRule(WHITESPACE); // FIXME Set to document

    return b;
  }
}
