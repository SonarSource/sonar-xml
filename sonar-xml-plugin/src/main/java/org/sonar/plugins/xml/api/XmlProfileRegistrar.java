/*
 * SonarQube XML Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.api;

import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.ServerSide;

import java.util.Collection;

/**
 * This class can be extended to provide additional rule keys in the builtin default quality profile.
 *
 * <pre>
 *   {@code
 *     public void register(RegistrarContext registrarContext) {
 *       registrarContext.registerDefaultQualityProfileRules(ruleKeys);
 *     }
 *   }
 * </pre>
 *
 */
@ServerSide
public interface XmlProfileRegistrar {

  /**
   * This method is called on server side and during an analysis to modify the builtin default quality profile for Xml.
   */
  void register(RegistrarContext registrarContext);

  interface RegistrarContext {
    /**
     * Registers additional rules into the "Sonar Way" default quality profile for Xml.
     *
     * @param ruleKeys additional rule keys
     */
    void registerDefaultQualityProfileRules(Collection<RuleKey> ruleKeys);
  }

}
