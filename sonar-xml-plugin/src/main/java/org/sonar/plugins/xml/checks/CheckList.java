/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import org.sonar.plugins.xml.checks.hibernate.DatabaseSchemaUpdateCheck;
import org.sonar.plugins.xml.checks.security.HardcodedCredentialsCheck;
import org.sonar.plugins.xml.checks.security.android.AndroidExportedContentPermissionsCheck;
import org.sonar.plugins.xml.checks.security.android.AndroidPermissionsCheck;
import org.sonar.plugins.xml.checks.security.android.DebugFeatureCheck;
import org.sonar.plugins.xml.checks.security.web.BasicAuthenticationCheck;
import org.sonar.plugins.xml.checks.security.web.CrossOriginResourceSharingCheck;
import org.sonar.plugins.xml.checks.security.web.HttpOnlyOnCookiesCheck;
import org.sonar.plugins.xml.checks.struts.ActionNumberCheck;
import org.sonar.plugins.xml.checks.security.web.ValidationFiltersCheck;

public class CheckList {

  private CheckList() {
  }

  public static List<Class<?>> getCheckClasses() {
    return Arrays.asList(
      ActionNumberCheck.class,
      AndroidExportedContentPermissionsCheck.class,
      AndroidPermissionsCheck.class,
      CharBeforePrologCheck.class,
      CrossOriginResourceSharingCheck.class,
      DatabaseSchemaUpdateCheck.class,
      DefaultInterceptorsLocationCheck.class,
      DebugFeatureCheck.class,
      TabCharacterCheck.class,
      HardcodedCredentialsCheck.class,
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
      CommentedOutCodeCheck.class
    );
  }

}
