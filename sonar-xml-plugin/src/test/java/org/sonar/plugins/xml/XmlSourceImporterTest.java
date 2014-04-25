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
package org.sonar.plugins.xml;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.Language;
import org.sonar.plugins.xml.language.Xml;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class XmlSourceImporterTest {

  Xml xml;
  private XmlSourceImporter importer;

  @Before
  public void setUp() {
    xml = new Xml();
    importer = new XmlSourceImporter(xml);
  }

  @Test
  public void testCreateImporter() throws Exception {
    assertThat(importer.getLanguage(), is((Language) xml));
  }

  @Test
  public void testToString() throws Exception {
    assertNotNull(importer.toString());
  }
}
