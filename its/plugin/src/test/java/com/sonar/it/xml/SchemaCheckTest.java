/*
 * XML :: IT
 * Copyright (C) 2013 ${owner}
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonar.it.xml;

import com.google.common.collect.ImmutableMap;
import com.sonar.orchestrator.Orchestrator;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.SonarClient;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaCheckTest {

  private static final String PROJECT_KEY = "schema-check";

  @ClassRule
  public static Orchestrator orchestrator = XmlTestSuite.ORCHESTRATOR;

  @Test
  public void test() {
    String ruleKey = "schemaCheck1";
    SonarClient sonarClient = orchestrator.getServer().adminWsClient();
    sonarClient.post("/api/rules/create", ImmutableMap.<String, Object>builder()
      .put("name", "rule1")
      .put("markdown_description", "rule1")
      .put("severity", "INFO")
      .put("status", "READY")
      .put("template_key", "xml:XmlSchemaCheck")
      .put("custom_key", ruleKey)
      .put("prevent_reactivation", "true")
      .put("params", "schemas=autodetect")
      .build());
    String profiles = sonarClient.get("api/rules/app");
    Pattern pattern = Pattern.compile("xml-empty-\\d+");
    Matcher matcher = pattern.matcher(profiles);
    assertThat(matcher.find());
    String profilekey = matcher.group();
    sonarClient.post("api/qualityprofiles/activate_rule",
      "profile_key", profilekey,
      "rule_key", "xml:" + ruleKey,
      "severity", "INFO",
      "params", "");

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

}
