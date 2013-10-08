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

import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.profiles.AnnotationProfileParser;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Languages;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.rules.XmlSonarWayProfile;

import javax.xml.XMLConstants;

import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Matthijs Galesloot
 */
public class AbstractXmlPluginTester {

  static {
    System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
        "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
  }

  private static MavenProject loadPom(File pomFile) throws URISyntaxException {

    FileReader fileReader = null;
    try {
      fileReader = new FileReader(pomFile);
      Model model = new MavenXpp3Reader().read(fileReader);
      MavenProject project = new MavenProject(model);
      project.setFile(pomFile);
      project.addCompileSourceRoot(project.getBuild().getSourceDirectory());

      return project;
    } catch (Exception e) {
      throw new SonarException("Failed to read Maven project file : " + pomFile.getPath(), e);
    } finally {
      IOUtils.closeQuietly(fileReader);
    }
  }

  /**
   * create standard rules profile
   */
  protected RulesProfile createStandardRulesProfile() {
    ProfileDefinition profileDefinition = getProfileDefinition();

    ValidationMessages messages = ValidationMessages.create();
    RulesProfile profile = profileDefinition.createProfile(messages);
    assertEquals(0, messages.getErrors().size());
    assertEquals(0, messages.getWarnings().size());
    assertEquals(0, messages.getInfos().size());
    return profile;
  }

  protected XmlSonarWayProfile getProfileDefinition() {
    RuleFinder ruleFinder = ruleFinder();
    return new XmlSonarWayProfile(new AnnotationProfileParser(ruleFinder));
  }

  protected Project loadProjectFromPom(File pomFile) throws URISyntaxException {
    MavenProject pom = loadPom(pomFile);
    Project project = new Project(pom.getGroupId() + ":" + pom.getArtifactId()).setPom(pom).setConfiguration(
        new MapConfiguration(pom.getProperties()));
    project.setFileSystem(new DefaultProjectFileSystem(project, new Languages(new Xml())));
    project.setPom(pom);
    project.setLanguageKey(Xml.INSTANCE.getKey());
    project.setLanguage(Xml.INSTANCE);

    return project;
  }

  static RuleFinder ruleFinder() {
    return when(mock(RuleFinder.class).findByKey(Mockito.anyString(), Mockito.anyString())).thenAnswer(new Answer<Rule>() {
      public Rule answer(InvocationOnMock invocation) {
        Object[] arguments = invocation.getArguments();
        return Rule.create((String) arguments[0], (String) arguments[1], (String) arguments[1]).setDescription("Mocked description");
      }
    }).getMock();
  }

}
