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
package org.sonar.plugins.xml.checks;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.AbstractXmlPluginTester;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractCheckTester extends AbstractXmlPluginTester {

  @org.junit.Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  private void configureDefaultParams(AbstractXmlCheck check, Rule rule) {
    ValidationMessages validationMessages = ValidationMessages.create();
    RulesProfile rulesProfile = getProfileDefinition().createProfile(validationMessages);

    rulesProfile.activateRule(rule, null);
    ActiveRule activeRule = rulesProfile.getActiveRule(rule);

    assertNotNull("Could not find activeRule", activeRule);

    try {
      if (activeRule.getActiveRuleParams() != null) {
        for (ActiveRuleParam param : activeRule.getActiveRuleParams()) {
          Object value = PropertyUtils.getProperty(check, param.getRuleParam().getKey());
          if (value instanceof Integer) {
            value = Integer.parseInt(param.getValue());
          } else {
            value = param.getValue();
          }
          PropertyUtils.setProperty(check, param.getRuleParam().getKey(), value);
        }
      }
    } catch (IllegalAccessException e) {
      throw new SonarException(e);
    } catch (InvocationTargetException e) {
      throw new SonarException(e);
    } catch (NoSuchMethodException e) {
      throw new SonarException(e);
    }
  }

  private Rule getRule(String ruleKey, Class<? extends AbstractXmlCheck> checkClass) {

    AnnotationRuleParser parser = new AnnotationRuleParser();
    List<Rule> rules = parser.parse(CheckRepository.REPOSITORY_KEY, Arrays.asList(new Class[]{checkClass}));
    for (Rule rule : rules) {
      if (rule.getKey().equals(ruleKey)) {
        return rule;
      }
    }
    return null;
  }

  protected AbstractXmlCheck instantiateCheck(Class<? extends AbstractXmlCheck> checkClass, String... params) {
    try {
      AbstractXmlCheck check = checkClass.newInstance();
      String ruleKey = checkClass.getAnnotation(org.sonar.check.Rule.class).key();

      Rule rule = getRule(ruleKey, checkClass);
      assertNotNull("Could not find rule", rule);
      check.setRule(rule);
      configureDefaultParams(check, rule);

      for (int i = 0; i < params.length / 2; i++) {
        BeanUtils.setProperty(check, params[i * 2], params[i * 2 + 1]);
        assertNotNull(BeanUtils.getProperty(check, params[i * 2]));
      }
      return check;
    } catch (IllegalAccessException e) {
      throw new SonarException(e);
    } catch (InstantiationException e) {
      throw new SonarException(e);
    } catch (InvocationTargetException e) {
      throw new SonarException(e);
    } catch (NoSuchMethodException e) {
      throw new SonarException(e);
    }
  }

  protected XmlSourceCode parseAndCheck(Reader reader, Class<? extends AbstractXmlCheck> checkClass, String... params) {

    return parseAndCheck(reader, null, null, checkClass, params);
  }

  protected XmlSourceCode parseAndCheck(Reader reader, java.io.File file, String code, Class<? extends AbstractXmlCheck> checkClass,
                                        String... params) {

    AbstractXmlCheck check = instantiateCheck(checkClass, params);

    XmlSourceCode xmlSourceCode = new XmlSourceCode(new File(file == null ? "test" : file.getPath()), file);
    xmlSourceCode.setCode(code);

    if (xmlSourceCode.parseSource(mockFileSystem())) {
      check.validate(xmlSourceCode);
    }

    return xmlSourceCode;
  }

  protected void parseCheckAndAssert(String fragment, Class<? extends AbstractXmlCheck> clazz, int numViolations, String... params) {
    Reader reader = new StringReader(fragment);
    XmlSourceCode sourceCode = parseAndCheck(reader, null, fragment, clazz, params);

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, numViolations, sourceCode.getXmlIssues().size());
  }

  protected ModuleFileSystem mockFileSystem() {
    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.sourceCharset()).thenReturn(Charset.defaultCharset());
    when(fs.workingDir()).thenReturn(temporaryFolder.newFolder("temp"));

    return fs;
  }

}
