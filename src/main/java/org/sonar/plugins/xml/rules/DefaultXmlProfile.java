/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
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

package org.sonar.plugins.xml.rules;

import java.util.List;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.language.Xml;

/**
 * Default XML profile.
 * 
 * @author Matthijs Galesloot
 * @since 1.0
 */
public final class DefaultXmlProfile extends ProfileDefinition {

  private final XmlRulesRepository xmlRulesRepository;
  private final XmlMessagesRepository xmlMessagesRepository;
  private final XmlSchemaMessagesRepository xmlSchemaMessagesRepository;

  public DefaultXmlProfile(XmlRulesRepository xmlRulesRepository, XmlMessagesRepository xmlMessagesRepository,
      XmlSchemaMessagesRepository xmlSchemaMessagesRepository) {
    this.xmlRulesRepository = xmlRulesRepository;
    this.xmlMessagesRepository = xmlMessagesRepository;
    this.xmlSchemaMessagesRepository = xmlSchemaMessagesRepository;
  }

  private void addMessageRepository(RulesProfile rulesProfile, AbstractMessagesRepository messagesRepository) {
    List<Rule> rules = messagesRepository.createRules();
    for (Rule rule : rules) {
      rulesProfile.activateRule(rule, RulePriority.MAJOR);
    }
  }

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    List<Rule> rules = xmlRulesRepository.createRules();
    RulesProfile rulesProfile = RulesProfile.create("Default XML Profile", Xml.KEY);
    for (Rule rule : rules) {
      rulesProfile.activateRule(rule, null);
    }
    addMessageRepository(rulesProfile, xmlMessagesRepository);
    addMessageRepository(rulesProfile, xmlSchemaMessagesRepository);
    rulesProfile.setDefaultProfile(true);
    return rulesProfile;
  }
}
