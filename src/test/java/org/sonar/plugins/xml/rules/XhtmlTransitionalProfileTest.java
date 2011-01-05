package org.sonar.plugins.xml.rules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.AbstractXmlPluginTester;


public class XhtmlTransitionalProfileTest extends AbstractXmlPluginTester {

  @Test
  public void testCreateProfile() {
    XhtmlTransitionalProfile profile = new XhtmlTransitionalProfile(getProfileDefinition());
    ValidationMessages messages = ValidationMessages.create();
    profile.createProfile(messages);
    assertEquals(0, messages.getErrors().size());
    assertEquals(0, messages.getWarnings().size());
    assertEquals(0, messages.getInfos().size());
  }
}
