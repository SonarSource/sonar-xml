/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.plugins.xml.checks.security.android;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;

@Rule(key = "S5604")
public class AndroidPermissionsCheck extends AbstractAndroidManifestCheck {

  private static final String MESSAGE = "Make sure the use of \"%s\" permission is necessary.";
  private final XPathExpression xPathExpression = XPathBuilder.forExpression("/manifest/uses-permission/@n1:name")
    .withNamespace("n1", "http://schemas.android.com/apk/res/android")
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
      .filter(node -> DANGEROUS_PERMISSIONS.contains(node.getNodeValue()))
      .forEach(node -> reportIssue(node, String.format(MESSAGE, simpleName(node.getNodeValue()))));
  }

  private static String simpleName(String fullyQualifiedName) {
    return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
  }

}
