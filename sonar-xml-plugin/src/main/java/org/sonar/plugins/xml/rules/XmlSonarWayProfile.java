/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.rules;

import org.sonar.api.profiles.AnnotationProfileParser;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.language.Xml;

/**
 * Default XML profile.
 * 
 * @author Matthijs Galesloot
 */
public final class XmlSonarWayProfile extends ProfileDefinition {

  private final AnnotationProfileParser annotationProfileParser;

  public XmlSonarWayProfile(AnnotationProfileParser annotationProfileParser) {
    this.annotationProfileParser = annotationProfileParser;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    return annotationProfileParser.parse(
        CheckRepository.REPOSITORY_KEY,
        CheckRepository.SONAR_WAY_PROFILE_NAME,
        Xml.KEY,
        CheckRepository.getCheckClasses(),
        validation);
  }
}
