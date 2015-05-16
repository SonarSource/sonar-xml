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
package org.sonar.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.sonar.channel.CodeReader;
import org.sonar.colorizer.HtmlCodeBuilder;
import org.sonar.colorizer.Tokenizer;

/**
 * This tokenizer takes care of DOCTYPE elements including declaration of internal DTDs, external DTDs and a 
 * combination of both.
 *
 * @author Ren&eacute; Wolfert
 */
public class DoctypeTokenizer extends Tokenizer {

  private static enum DoctypeParts {
    Doctype("<!DOCTYPE", ">"),
    DoctypeInternal("[", "]"),
    Element("<!ELEMENT", ">"),
    AttList("<!ATTLIST", ">");

    private final String selectorStart;
    private final String selectorEnd;

    private final char[] selectorStartChars;
    private final char[] selectorEndChars;

    private DoctypeParts(final String selectorStart, final String selectorEnd) {
      this.selectorStart = selectorStart;
      this.selectorEnd = selectorEnd;

      this.selectorStartChars = selectorStart.toCharArray();
      this.selectorEndChars = selectorEnd.toCharArray();
    }

    public String getSelectorStart() {
      return selectorStart;
    }

    public String getSelectorEnd() {
      return selectorEnd;
    }

    public boolean matchesStartSelector(final CodeReader code) {
      return code.peek() == selectorStartChars[0]
          && Arrays.equals(code.peek(selectorStartChars.length), selectorStartChars);
    }

    public boolean matchesEndSelector(final CodeReader code) {
      return code.peek() == selectorEndChars[0] && Arrays.equals(code.peek(selectorEndChars.length), selectorEndChars);
    }
  }

  private static final String DOCTYPE_PART = "DOCTYPE_PARTS";
  
  private final String tagBefore;
  private final String tagAfter;

  public DoctypeTokenizer(final String tagBefore, final String tagAfter) {
    this.tagBefore = tagBefore;
    this.tagAfter = tagAfter;
  }

  @Override
  public boolean consume(final CodeReader code, final HtmlCodeBuilder codeBuilder) {
    final boolean consumed;
    
    if (getCurrentOpenedDoctypePart(codeBuilder) == null 
        && DoctypeParts.Doctype.matchesStartSelector(code)) {
      addOpenedDoctypePart(codeBuilder, DoctypeParts.Doctype);

      codeBuilder.appendWithoutTransforming(tagBefore);
      // Consume DOCTYPE start
      code.popTo(Pattern.compile(Pattern.quote(DoctypeParts.Doctype.getSelectorStart())).matcher(""), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      consumed = true;

    } else if (DoctypeParts.Doctype == getCurrentOpenedDoctypePart(codeBuilder)
        && DoctypeParts.DoctypeInternal.matchesStartSelector(code)) {
      addOpenedDoctypePart(codeBuilder, DoctypeParts.DoctypeInternal);
      consumed = true;

    } else if (DoctypeParts.DoctypeInternal == getCurrentOpenedDoctypePart(codeBuilder)
        && DoctypeParts.Element.matchesStartSelector(code)) {
      addOpenedDoctypePart(codeBuilder, DoctypeParts.Element);

      codeBuilder.appendWithoutTransforming(tagBefore);

      // Consume ELEMENT start
      code.popTo(Pattern.compile(Pattern.quote(DoctypeParts.Element.getSelectorStart())).matcher(""), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      consumed = true;

    } else if (DoctypeParts.DoctypeInternal == getCurrentOpenedDoctypePart(codeBuilder)
        && DoctypeParts.AttList.matchesStartSelector(code)) {
      addOpenedDoctypePart(codeBuilder, DoctypeParts.AttList);

      codeBuilder.appendWithoutTransforming(tagBefore);

      // Consume ATTLIST start
      code.popTo(Pattern.compile(Pattern.quote(DoctypeParts.AttList.getSelectorStart())).matcher(""), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      consumed = true;

    } else if (DoctypeParts.AttList == getCurrentOpenedDoctypePart(codeBuilder)
        && DoctypeParts.AttList.matchesEndSelector(code)) {
      removeOpenedDoctypePart(codeBuilder, DoctypeParts.AttList);

      codeBuilder.appendWithoutTransforming(tagBefore);
      // Consume ATTLIST end
      code.popTo(Pattern.compile(Pattern.quote(DoctypeParts.AttList.getSelectorEnd())).matcher(""), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      consumed = true;

    } else if (DoctypeParts.Element == getCurrentOpenedDoctypePart(codeBuilder)
        && DoctypeParts.Element.matchesEndSelector(code)) {
      removeOpenedDoctypePart(codeBuilder, DoctypeParts.Element);

      codeBuilder.appendWithoutTransforming(tagBefore);
      // Consume ELEMENT end
      code.popTo(Pattern.compile(Pattern.quote(DoctypeParts.Element.getSelectorEnd())).matcher(""), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      consumed = true;

    } else if (DoctypeParts.DoctypeInternal == getCurrentOpenedDoctypePart(codeBuilder)
        && DoctypeParts.DoctypeInternal.matchesEndSelector(code)) {
      removeOpenedDoctypePart(codeBuilder, DoctypeParts.DoctypeInternal);
      consumed = true;

    } else if (DoctypeParts.Doctype == getCurrentOpenedDoctypePart(codeBuilder)
        && DoctypeParts.Doctype.matchesEndSelector(code)) {
      removeOpenedDoctypePart(codeBuilder, DoctypeParts.Doctype);

      codeBuilder.appendWithoutTransforming(tagBefore);
      // Consume DOCTYPE end
      code.popTo(Pattern.compile(Pattern.quote(DoctypeParts.Doctype.getSelectorEnd())).matcher(""), codeBuilder);
      codeBuilder.appendWithoutTransforming(tagAfter);
      consumed = true;

    } else if (getCurrentOpenedDoctypePart(codeBuilder) != null) {
      code.pop(codeBuilder);
      consumed = true;

    } else {
      consumed = false;
    }

    return consumed;
  }

  private DoctypeParts getCurrentOpenedDoctypePart(final HtmlCodeBuilder codeBuilder) {
    final List<DoctypeParts> types = (List<DoctypeParts>) codeBuilder.getVariable(DoctypeTokenizer.DOCTYPE_PART, null);

    final DoctypeParts openedDoctypePart;
    if (types != null && !types.isEmpty()) {
      openedDoctypePart = types.get(types.size() - 1);
    } else {
      openedDoctypePart = null;
    }

    return openedDoctypePart;
  }

  private void addOpenedDoctypePart(final HtmlCodeBuilder codeBuilder, final DoctypeParts type) {
    final List<DoctypeParts> existingTypes = (List<DoctypeParts>) codeBuilder.getVariable(DoctypeTokenizer.DOCTYPE_PART, null);

    final List<DoctypeParts> types = existingTypes != null ? existingTypes : new ArrayList<DoctypeParts>();

    types.add(type);

    codeBuilder.setVariable(DoctypeTokenizer.DOCTYPE_PART, types);
  }

  private void removeOpenedDoctypePart(final HtmlCodeBuilder codeBuilder, final DoctypeParts type) {
    final List<DoctypeParts> existingTypes = (List<DoctypeParts>) codeBuilder.getVariable(DoctypeTokenizer.DOCTYPE_PART, null);

    final List<DoctypeParts> types = existingTypes != null ? existingTypes : new ArrayList<DoctypeParts>();

    if (!types.isEmpty()) {
      types.remove(types.size() - 1);
    }

    codeBuilder.setVariable(DoctypeTokenizer.DOCTYPE_PART, types);
  }
}
