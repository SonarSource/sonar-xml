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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import org.junit.Test;
import org.sonar.colorizer.CodeColorizer;

public class DoctypeTokenizerTest {

  private final CodeColorizer codeColorizer = new CodeColorizer(XmlColorizer.createTokenizers());

  private String highlight(final String webSourceCode) {
    return codeColorizer.toHtml(new StringReader(webSourceCode));
  }

  @Test
  public void testHighlightSingleLineDoctypeExternalDTD() {
    final String input = "<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\">";

    final String highlightedText = highlight(input);
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!DOCTYPE</span> web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\"<span class=\"j\">&gt;</span>"));
  }

  @Test
  public void testHighlightMultiLineDoctypeExternalDTD() {
    final String input = "<!DOCTYPE \r\n"
        + "web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \r\n"
        + "\"http://java.sun.com/dtd/web-app_2_3.dtd\">";

    final String highlightedText = highlight(input);
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!DOCTYPE</span>"));
    assertThat(highlightedText, containsString("<pre>web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" </pre>"));
    assertThat(highlightedText, containsString("<pre>\"http://java.sun.com/dtd/web-app_2_3.dtd\"<span class=\"j\">&gt;</span></pre>"));
  }

  @Test
  public void testHighlightSingleLineDoctypeInternalDTD() {
    final String input = "<!DOCTYPE web-app [<!ELEMENT web-app (foo,bar)><!ELEMENT foo (#PCDATA)><!ELEMENT bar (#PCDATA)><!ATTLIST bar bar_value (A | B |C) #IMPLIED>]>";

    final String highlightedText = highlight(input);
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!DOCTYPE</span> web-app [<span class=\"j\">&lt;!ELEMENT</span> web-app (foo,bar)<span class=\"j\">&gt;</span><span class=\"j\">&lt;!ELEMENT</span> foo (#PCDATA)<span class=\"j\">&gt;</span><span class=\"j\">&lt;!ELEMENT</span> bar (#PCDATA)<span class=\"j\">&gt;</span><span class=\"j\">&lt;!ATTLIST</span> bar bar_value (A | B |C) #IMPLIED<span class=\"j\">&gt;</span>]<span class=\"j\">&gt;</span>"));
  }

  @Test
  public void testHighlightMultiLineDoctypeInternalDTD() {
    final String input = "<!DOCTYPE web-app [\r\n" + "<!ELEMENT web-app (foo,bar)>\r\n"
        + "<!ELEMENT foo (#PCDATA)>\r\n" + "<!ELEMENT bar (#PCDATA)>\r\n"
        + "<!ATTLIST bar bar_value (A | B |C) #IMPLIED>\r\n" + "]>";

    final String highlightedText = highlight(input);
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!DOCTYPE</span> web-app ["));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ELEMENT</span> web-app (foo,bar)<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ELEMENT</span> foo (#PCDATA)<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ELEMENT</span> bar (#PCDATA)<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ATTLIST</span> bar bar_value (A | B |C) #IMPLIED<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("]<span class=\"j\">&gt;</span>"));
  }

  @Test
  public void testHighlightMultiLineWithXmlCommentDoctypeInternalDTD() {
    final String input = "<!DOCTYPE web-app [\r\n" + "<!ELEMENT web-app (foo,bar)>\r\n"
        + "<!ELEMENT foo (#PCDATA)>\r\n" + "<!-- Comment -->\r\n" + "<!ELEMENT bar (#PCDATA)>\r\n"
        + "<!ATTLIST bar bar_value (A | B |C) #IMPLIED>\r\n" + "]>";

    final String highlightedText = highlight(input);
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!DOCTYPE</span> web-app ["));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ELEMENT</span> web-app (foo,bar)<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ELEMENT</span> foo (#PCDATA)<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ELEMENT</span> bar (#PCDATA)<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ATTLIST</span> bar bar_value (A | B |C) #IMPLIED<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("]<span class=\"j\">&gt;</span>"));
  }

  @Test
  public void testHighlightMultiLineDoctypeInternalAndExternalDTD() {
    final String input = "<!DOCTYPE web-app SYSTEM \"web-app.dtd\" [" + "<!ELEMENT web-app (foo,bar)>\r\n"
        + "<!ELEMENT foo (#PCDATA)>\r\n" + "<!ELEMENT bar (#PCDATA)>\r\n"
        + "<!ATTLIST bar bar_value (A | B |C) #IMPLIED>\r\n" + "]>";

    final String highlightedText = highlight(input);
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!DOCTYPE</span> web-app SYSTEM \"web-app.dtd\" ["));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ELEMENT</span> web-app (foo,bar)<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ELEMENT</span> foo (#PCDATA)<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ELEMENT</span> bar (#PCDATA)<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("<span class=\"j\">&lt;!ATTLIST</span> bar bar_value (A | B |C) #IMPLIED<span class=\"j\">&gt;</span>"));
    assertThat(highlightedText, containsString("]<span class=\"j\">&gt;</span>"));

  }
}
