/*
 * SonarQube XML Plugin
 * Copyright (C) 2013-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sonar.it.xml;

import com.sonar.orchestrator.Orchestrator;
import java.io.File;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonarqube.ws.QualityProfiles;
import org.sonarqube.ws.client.PostRequest;
import org.sonarqube.ws.client.qualityprofile.SearchWsRequest;

import static com.sonar.it.xml.XmlTestSuite.newAdminWsClient;
import static com.sonar.it.xml.XmlTestSuite.newWsClient;
import static java.lang.String.format;
import static org.fest.assertions.Assertions.assertThat;

public class SchemaCheckTest {

  private static final String PROJECT_KEY = "schema-check";

  @ClassRule
  public static Orchestrator orchestrator = XmlTestSuite.ORCHESTRATOR;

  @Test
  public void test() {
    String ruleKey = "schemaCheck1";
    createAndActivateRuleFromTemplate(ruleKey);

    orchestrator.getServer().provisionProject(PROJECT_KEY, PROJECT_KEY);
    orchestrator.getServer().associateProjectToQualityProfile(PROJECT_KEY, "xml", "empty");
    orchestrator.executeBuild(XmlTestSuite.createSonarRunner()
      .setProjectDir(new File("projects/schema-check"))
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_KEY)
      .setProjectVersion("1.0")
      .setSourceDirs("."));

    IssueQuery query = IssueQuery.create().componentRoots(PROJECT_KEY);
    List<Issue> issues = orchestrator.getServer().wsClient().issueClient().find(query).list();
    assertThat(issues).hasSize(1);
    Issue issue = issues.get(0);
    assertThat(issue.componentKey()).isEqualTo(PROJECT_KEY + ":invalid-html.xml");
    assertThat(issue.line()).isEqualTo(7);
  }

  private void createAndActivateRuleFromTemplate(String ruleKey) {
    String language = "xml";
    String qualityProfileName = "empty";
    newAdminWsClient().wsConnector().call(new PostRequest("api/rules/create")
      .setParam("name", "rule1")
      .setParam("markdown_description", "rule1")
      .setParam("severity", "INFO")
      .setParam("status", "READY")
      .setParam("template_key", "xml:XmlSchemaCheck")
      .setParam("custom_key", ruleKey)
      .setParam("prevent_reactivation", "true")
      .setParam("params", "schemas=autodetect")).failIfNotSuccessful();

    QualityProfiles.SearchWsResponse.QualityProfile qualityProfile = newWsClient().qualityProfiles().search(new SearchWsRequest()).getProfilesList().stream()
      .filter(qp -> qp.getLanguage().equals(language))
      .filter(qp -> qp.getName().equals(qualityProfileName))
      .findFirst().orElseThrow(() -> new IllegalStateException(format("Could not find quality profile '%s' for language '%s' ", qualityProfileName, language)));
    String profileKey = qualityProfile.getKey();

    newAdminWsClient().wsConnector().call(new PostRequest("api/qualityprofiles/activate_rule")
      .setParam("profile_key", profileKey)
      .setParam("rule_key", "xml:" + ruleKey)
      .setParam("severity", "INFO")).failIfNotSuccessful();
  }

}
