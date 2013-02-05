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

  YES,
  NO,

  DOCUMENT,

  CHAR,

  S,
  NAME,
  ENTITY_VALUE,
  ATT_VALUE,
  SYSTEM_LITERAL,
  PUBID_LITERAL,

  CHAR_DATA,

  COMMENT,

  PI,
  PI_TARGET,

  CD_SECT,
  CD_START,
  CDATA,
  CD_END,

  PROLOG,
  XML_DECL,
  VERSION_INFO,
  EQ,
  VERSION_NUM,
  MISC,
  DOC_TYPE_DECL,
  DECL_SEP,
  INT_SUBSET,
  MARKUP_DECL,
  EXT_SUBSET,
  EXT_SUBSET_DECL,

  SD_DECL,

  ELEMENT,

  S_TAG,
  ATTRIBUTE,
  E_TAG,
  CONTENT,
  EMPTY_ELEM_TAG,

  ELEMENT_DECL,
  CONTENT_SPEC,
  CHILDREN,
  CP,
  CHOICE,
  SEQ,
  MIXED,
  ATT_LIST_DECL,
  ATT_DEF,
  ATT_TYPE,
  STRING_TYPE,
  TOKENIZED_TYPE,
  ENUMERATED_TYPE,
  NOTATION_TYPE,
  ENUMERATION,
  DEFAULT_DECL,
  CONDITIONAL_SECT,
  INCLUDE_SECT,
  IGNORE_SECT,
  IGNORE_SECT_CONTENTS,
  IGNORE,

  CHAR_REF,
  REFERENCE,
  ENTITY_REF,
  PE_REFERENCE,

  ENCODING_DECL,
  EXTERNAL_ID,
  ENTITY_DECL,
  NOTATION_DECL,
  TEXT_DECL,

  ;

  private static final String CHAR_REGEXP = "[\u0001-\uD7FF\uE000-\uFFFD]";

  private static final String S_REGEXP = "[ \t\r\n]++";
  private static final String NAME_START_CHAR_REGEXP = "[:A-Z_a-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D" +
    "\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]";
  private static final String NAME_CHAR_REGEXP = "(?:" + NAME_START_CHAR_REGEXP + "|[-.0-9\u00B7\u0300-\u036F\u203F-\u2040])";
  private static final String NAME_REGEXP = NAME_START_CHAR_REGEXP + NAME_CHAR_REGEXP + "*+";
  private static final String PUBID_CHAR_REGEXP = "[ \r\na-zA-Z0-9-'()+,./:=?;!*#@$_%]";

  private static final String COMMENT_REGEXP = "<!--" + "(?:(?!-)" + CHAR_REGEXP + "|-" + "(?!-)" + CHAR_REGEXP + ")*+" + "-->";

  private static final String CHAR_REF_REGEXP = "(?:" + "&#" + "(?:[0-9]++|x[0-9a-fA-F]++);" + ")";

  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    b.rule(YES).is("yes");
    b.rule(NO).is("no");

    // b.rule(DOCUMENT).is(PROLOG, ELEMENT, b.zeroOrMore(MISC));

    b.rule(CHAR).is(b.regexp(CHAR_REGEXP));

    b.rule(S).is(b.skippedTrivia(b.regexp(S_REGEXP))).skip();
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

    b.rule(CHAR_DATA).is(b.regexp("[^<&]*+"));

    b.rule(COMMENT).is(b.commentTrivia(b.regexp(COMMENT_REGEXP))).skip();

    b.rule(PI).is("<?", PI_TARGET, b.optional(S, b.regexp("((?!\\?>)" + CHAR_REGEXP + ")++")), "?>");
    b.rule(PI_TARGET).is(
        b.nextNot(
            b.firstOf('x', 'X'),
            b.firstOf('m', 'M'),
            b.firstOf('l', 'L')),
        NAME);

    b.rule(CD_SECT).is(CD_START, CDATA, CD_END);
    b.rule(CD_START).is("<![CDATA[");
    b.rule(CDATA).is(b.regexp("(?:(?!]]>)" + CHAR_REGEXP + ")*+"));
    b.rule(CD_END).is("]]>");

    b.rule(PROLOG).is(b.optional(XML_DECL), b.zeroOrMore(MISC), b.optional(DOC_TYPE_DECL, b.zeroOrMore(MISC))); // TODO Test
    b.rule(XML_DECL).is("<?xml", VERSION_INFO, b.optional(ENCODING_DECL), b.optional(SD_DECL), b.optional(S), "?>"); // TODO Test
    b.rule(VERSION_INFO).is(
        S, "version", EQ,
        b.firstOf(
            b.sequence('"', VERSION_NUM, '"'),
            b.sequence('\'', VERSION_NUM, '\'')));
    b.rule(EQ).is(b.optional(S), '=', b.optional(S));
    b.rule(VERSION_NUM).is("1.", b.regexp("[0-9]++"));
    b.rule(MISC).is(
        b.firstOf(
            S,
            COMMENT,
            PI));
    b.rule(DOC_TYPE_DECL).is("<!DOCTYPE", S, NAME, b.optional(S, EXTERNAL_ID), b.optional(S), b.optional('[', INT_SUBSET, ']', b.optional(S)), '>'); // TODO
    b.rule(DECL_SEP).is(
        b.firstOf(
            S,
            PE_REFERENCE));
    b.rule(INT_SUBSET).is( // TODO Test
        b.zeroOrMore(
            b.firstOf(
                MARKUP_DECL,
                DECL_SEP)));
    b.rule(MARKUP_DECL).is( // TODO Test
        b.firstOf(
            ELEMENT_DECL,
            ATT_LIST_DECL,
            ENTITY_DECL,
            NOTATION_DECL,
            PI,
            COMMENT));
    b.rule(EXT_SUBSET).is(b.optional(TEXT_DECL), EXT_SUBSET_DECL); // TODO Test
    b.rule(EXT_SUBSET_DECL).is( // TODO Test
        b.zeroOrMore(
            b.firstOf(
                MARKUP_DECL,
                CONDITIONAL_SECT,
                DECL_SEP)));

    b.rule(SD_DECL).is(
        S, "standalone", EQ,
        b.firstOf(
            b.sequence('"', b.firstOf(YES, NO), '"'),
            b.sequence('\'', b.firstOf(YES, NO), '\'')));

    b.rule(ELEMENT).is(
        b.firstOf(
            EMPTY_ELEM_TAG,
            b.sequence(S_TAG, CONTENT, E_TAG)));

    b.rule(S_TAG).is('<', NAME, b.zeroOrMore(S, ATTRIBUTE), b.optional(S), '>');
    b.rule(ATTRIBUTE).is(NAME, EQ, ATT_VALUE);
    b.rule(E_TAG).is("</", NAME, b.optional(S), '>');
    b.rule(CONTENT).is(
        b.optional(CHAR_DATA),
        b.zeroOrMore(
            b.firstOf(
                ELEMENT,
                REFERENCE,
                CD_SECT,
                PI,
                COMMENT),
            b.optional(CHAR_DATA)));
    b.rule(EMPTY_ELEM_TAG).is('<', NAME, b.zeroOrMore(S, ATTRIBUTE), b.optional(S), "/>");

    b.rule(ELEMENT_DECL).is("<!ELEMENT", S, NAME, S, CONTENT_SPEC, b.optional(S), '>');
    b.rule(CONTENT_SPEC).is(
        b.firstOf(
            "EMPTY",
            "ANY",
            MIXED,
            CHILDREN));
    b.rule(CHILDREN).is(
        b.firstOf(
            CHOICE,
            SEQ),
        b.optional(
            b.firstOf(
                '?',
                '*',
                '+')));
    b.rule(CP).is(
        b.firstOf(
            NAME,
            CHOICE,
            SEQ),
        b.optional(
            b.firstOf(
                '?',
                '*',
                '+')));
    b.rule(CHOICE).is('(', b.optional(S), CP, b.oneOrMore(b.optional(S), '|', b.optional(S), CP), b.optional(S), ')');
    b.rule(SEQ).is('(', b.optional(S), CP, b.zeroOrMore(b.optional(S), ',', b.optional(S), CP), b.optional(S), ')');
    b.rule(MIXED).is(
        b.firstOf(
            b.sequence('(', b.optional(S), "#PCDATA", b.zeroOrMore(b.optional(S), '|', b.optional(S), NAME), b.optional(S), ")*"),
            b.sequence('(', b.optional(S), "#PCDATA", b.optional(S), ')')));

    b.rule(CHAR_REF).is(b.regexp(CHAR_REF_REGEXP));
    b.rule(REFERENCE).is(b.firstOf(ENTITY_REF, CHAR_REF));
    b.rule(ENTITY_REF).is('&', NAME, ';');
    b.rule(PE_REFERENCE).is('%', NAME, ';');

    b.setRootRule(S); // FIXME Set to document

    return b;
  }
}
