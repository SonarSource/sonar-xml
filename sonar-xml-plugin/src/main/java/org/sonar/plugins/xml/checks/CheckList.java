/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.xml.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.plugins.xml.checks.ejb.DefaultInterceptorsLocationCheck;
import org.sonar.plugins.xml.checks.ejb.InterceptorExclusionsCheck;
import org.sonar.plugins.xml.checks.hibernate.DatabaseSchemaUpdateCheck;
import org.sonar.plugins.xml.checks.maven.ArtifactIdNamingConventionCheck;
import org.sonar.plugins.xml.checks.maven.DependencyWithSystemScopeCheck;
import org.sonar.plugins.xml.checks.maven.DeprecatedPomPropertiesCheck;
import org.sonar.plugins.xml.checks.maven.DisallowedDependenciesCheck;
import org.sonar.plugins.xml.checks.maven.GroupIdNamingConventionCheck;
import org.sonar.plugins.xml.checks.maven.PomElementOrderCheck;
import org.sonar.plugins.xml.checks.security.HardcodedCredentialsCheck;
import org.sonar.plugins.xml.checks.security.android.AndroidApplicationBackupCheck;
import org.sonar.plugins.xml.checks.security.android.AndroidCustomPermissionCheck;
import org.sonar.plugins.xml.checks.security.android.AndroidClearTextCheck;
import org.sonar.plugins.xml.checks.security.android.AndroidExportedContentPermissionsCheck;
import org.sonar.plugins.xml.checks.security.android.AndroidPermissionsCheck;
import org.sonar.plugins.xml.checks.security.android.AndroidProviderPermissionCheck;
import org.sonar.plugins.xml.checks.security.android.AndroidReceivingIntentsCheck;
import org.sonar.plugins.xml.checks.security.android.DebugFeatureCheck;
import org.sonar.plugins.xml.checks.security.web.BasicAuthenticationCheck;
import org.sonar.plugins.xml.checks.security.web.CrossOriginResourceSharingCheck;
import org.sonar.plugins.xml.checks.security.web.HttpOnlyOnCookiesCheck;
import org.sonar.plugins.xml.checks.security.web.ValidationFiltersCheck;
import org.sonar.plugins.xml.checks.spring.DefaultMessageListenerContainerCheck;
import org.sonar.plugins.xml.checks.spring.SingleConnectionFactoryCheck;
import org.sonar.plugins.xml.checks.struts.ActionNumberCheck;
import org.sonar.plugins.xml.checks.struts.FormNameDuplicationCheck;

public class CheckList {

  private CheckList() {
  }

  public static List<Class<?>> getCheckClasses() {
    return Arrays.asList(
      ArtifactIdNamingConventionCheck.class,
      DependencyWithSystemScopeCheck.class,
      DeprecatedPomPropertiesCheck.class,
      FormNameDuplicationCheck.class,
      PomElementOrderCheck.class,
      SingleConnectionFactoryCheck.class,
      ActionNumberCheck.class,
      AndroidClearTextCheck.class,
      AndroidCustomPermissionCheck.class,
      AndroidApplicationBackupCheck.class,
      AndroidExportedContentPermissionsCheck.class,
      AndroidPermissionsCheck.class,
      AndroidProviderPermissionCheck.class,
      AndroidReceivingIntentsCheck.class,
      CharBeforePrologCheck.class,
      CrossOriginResourceSharingCheck.class,
      DatabaseSchemaUpdateCheck.class,
      DefaultInterceptorsLocationCheck.class,
      DefaultMessageListenerContainerCheck.class,
      DebugFeatureCheck.class,
      GroupIdNamingConventionCheck.class,
      TabCharacterCheck.class,
      HardcodedCredentialsCheck.class,
      InterceptorExclusionsCheck.class,
      ParsingErrorCheck.class,
      NewlineCheck.class,
      IndentationCheck.class,
      XPathCheck.class,
      LineLengthCheck.class,
      TodoCommentCheck.class,
      HttpOnlyOnCookiesCheck.class,
      BasicAuthenticationCheck.class,
      FixmeCommentCheck.class,
      ValidationFiltersCheck.class,
      DisallowedDependenciesCheck.class,
      CommentedOutCodeCheck.class
    );
  }

}
