/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks.security.android;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Node;

import static org.sonar.plugins.xml.checks.security.android.Utils.ANDROID_MANIFEST_TOOLS;
import static org.sonar.plugins.xml.checks.security.android.Utils.ANDROID_MANIFEST_XMLNS;

@Rule(key = "S5604")
public class AndroidPermissionsCheck extends AbstractAndroidManifestCheck {

  private static final String MESSAGE = "Make sure the use of \"%s\" permission is necessary.";
  private final XPathExpression xPathExpression = XPathBuilder.forExpression("/manifest/uses-permission")
    .withNamespace("n1", ANDROID_MANIFEST_XMLNS)
    .build();

  private static final Set<String> DANGEROUS_PERMISSIONS = new HashSet<>(Arrays.asList(
    // the below list is dangerous
    // see https://developer.android.com/reference/android/Manifest.permission
    "android.permission.ACCEPT_HANDOVER",
    "android.permission.ACCESS_BACKGROUND_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_MEDIA_LOCATION",
    "android.permission.ACTIVITY_RECOGNITION",
    "com.android.voicemail.permission.ADD_VOICEMAIL",
    "android.permission.ANSWER_PHONE_CALLS",
    "android.permission.BODY_SENSORS",
    "android.permission.CALL_PHONE",
    "android.permission.CAMERA",
    "android.permission.GET_ACCOUNTS",
    "android.permission.PROCESS_OUTGOING_CALLS",
    "android.permission.READ_CALENDAR",
    "android.permission.READ_CALL_LOG",
    "android.permission.READ_CONTACTS",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.READ_PHONE_NUMBERS",
    "android.permission.READ_PHONE_STATE",
    "android.permission.READ_SMS",
    "android.permission.RECEIVE_MMS",
    "android.permission.RECEIVE_SMS",
    "android.permission.RECEIVE_WAP_PUSH",
    "android.permission.RECORD_AUDIO",
    "android.permission.SEND_SMS",
    "android.permission.USE_SIP",
    "android.permission.WRITE_CALENDAR",
    "android.permission.WRITE_CALL_LOG",
    "android.permission.WRITE_CONTACTS",
    "android.permission.WRITE_EXTERNAL_STORAGE",

    // the below list should not be used by third-party applications
    // see https://developer.android.com/reference/android/Manifest.permission
    "android.permission.ACCESS_CHECKIN_PROPERTIES",
    "android.permission.ACCOUNT_MANAGER",
    "android.permission.BIND_APPWIDGET",
    "android.permission.BLUETOOTH_PRIVILEGED",
    "android.permission.BROADCAST_PACKAGE_REMOVED",
    "android.permission.BROADCAST_SMS",
    "android.permission.BROADCAST_WAP_PUSH",
    "android.permission.CALL_PRIVILEGED",
    "android.permission.CAPTURE_AUDIO_OUTPUT",
    "android.permission.CHANGE_COMPONENT_ENABLED_STATE",
    "android.permission.CONTROL_LOCATION_UPDATES",
    "android.permission.DELETE_PACKAGES",
    "android.permission.DIAGNOSTIC",
    "android.permission.DUMP",
    "android.permission.FACTORY_TEST",
    "android.permission.INSTALL_LOCATION_PROVIDER",
    "android.permission.INSTALL_PACKAGES",
    "android.permission.LOCATION_HARDWARE",
    "android.permission.MASTER_CLEAR",
    "android.permission.MODIFY_PHONE_STATE",
    "android.permission.MOUNT_FORMAT_FILESYSTEMS",
    "android.permission.MOUNT_UNMOUNT_FILESYSTEMS",
    "android.permission.READ_INPUT_STATE",
    "android.permission.REBOOT",
    "android.permission.SEND_RESPOND_VIA_MESSAGE",
    "android.permission.SET_ALWAYS_FINISH",
    "android.permission.SET_ANIMATION_SCALE",
    "android.permission.SET_DEBUG_APP",
    "android.permission.SET_PROCESS_LIMIT",
    "android.permission.SET_TIME",
    "android.permission.SET_TIME_ZONE",
    "android.permission.SIGNAL_PERSISTENT_PROCESSES",
    "android.permission.STATUS_BAR",
    "android.permission.UPDATE_DEVICE_STATS",
    "android.permission.WRITE_APN_SETTINGS",
    "android.permission.WRITE_GSERVICES",
    "android.permission.WRITE_SECURE_SETTINGS"));

  @Override
  protected final void scanAndroidManifest(XmlFile file) {
    evaluateAsList(xPathExpression, file.getDocument()).stream()
      .filter(node -> !hasToolsNodeRemove(node))
      .filter(node -> DANGEROUS_PERMISSIONS.contains(findPermissionValue(node)))
      .forEach(node -> reportIssue(findPermissionNode(node), String.format(MESSAGE, simpleName(findPermissionValue(node)))));
  }

  private static String simpleName(String fullyQualifiedName) {
    return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
  }

  private static Node findPermissionNode(Node node) {
    return node.getAttributes().getNamedItemNS(ANDROID_MANIFEST_XMLNS, "name");
  }

  private static String findPermissionValue(Node node) {
    return findPermissionNode(node).getNodeValue();
  }

  private static boolean hasToolsNodeRemove(Node node) {
    Node toolsNodeAttribute = node.getAttributes().getNamedItemNS(ANDROID_MANIFEST_TOOLS, "node");
    return Optional.ofNullable(toolsNodeAttribute).map(n -> "remove".equals(n.getNodeValue())).orElse(false);
  }

}
