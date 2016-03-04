/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.xml.schemas;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Resolves references to XML schema's, if possible built-in.
 * 
 * @author Matthijs Galesloot
 */
public final class SchemaResolver implements LSResourceResolver {

  private static final String XHTML1_STRICT_XSD = "xhtml1/xhtml1-strict.xsd";
  private static final Logger LOG = LoggerFactory.getLogger(SchemaResolver.class);
  private static final Map<String, String> SCHEMAS_BUILTIN = new HashMap<>();
  private static final Map<String, String> DTD_BUILTIN = new HashMap<>();

  // SCHEMA's
  static {

    // XML
    SCHEMAS_BUILTIN.put("http://www.w3.org/2001/xml.xsd", "xml.xsd");
    SCHEMAS_BUILTIN.put("http://www.w3.org/XML/1998/namespace", "xml.xsd");

    // XHTML 1.0 - by Doctype
    SCHEMAS_BUILTIN.put("-//W3C//DTD XHTML 1.0 Strict//EN", XHTML1_STRICT_XSD);
    SCHEMAS_BUILTIN.put("-//W3C//DTD XHTML 1.0 Transitional//EN", "xhtml1/xhtml1-transitional.xsd");
    SCHEMAS_BUILTIN.put("-//W3C//DTD XHTML 1.0 Frameset//EN", "xhtml1/xhtml1-frameset.xsd");

    // XHTML 1.0 - by namespace
    SCHEMAS_BUILTIN.put("http://www.w3.org/1999/xhtml", XHTML1_STRICT_XSD);

    // XHTML 1.0 - by shortname
    SCHEMAS_BUILTIN.put("xhtml1-strict", XHTML1_STRICT_XSD);
    SCHEMAS_BUILTIN.put("xhtml1-transitional", "xhtml1/xhtml1-transitional.xsd");
    SCHEMAS_BUILTIN.put("xhtml1-frameset", "xhtml1/xhtml1-frameset.xsd");

    // XHTML 1.1 - by Doctype
    SCHEMAS_BUILTIN.put("-//W3C//DTD XHTML 1.1 Strict//EN", "xhtml11/xhtml11.xsd");

    // JSF Taglib to XSD conversions
    // (from http://blogger.ziesemer.com/2008/03/facelets-and-xsd-converted-tlds.html)
    SCHEMAS_BUILTIN.put("http://java.sun.com/jsf/core", "jsf/jsf-core-2.0.xsd");
    SCHEMAS_BUILTIN.put("http://java.sun.com/jsf/html", "jsf/html-basic-2.0.xsd");
    SCHEMAS_BUILTIN.put("http://java.sun.com/jsf/facelets", "jsf/facelets-ui-2.0.xsd");

    // MAVEN
    SCHEMAS_BUILTIN.put("http://maven.apache.org/POM/4.0.0", "maven/maven-4.0.0.xsd");
  }

  // DTDs
  static {
    // HTML 3.2
    DTD_BUILTIN.put("-//W3C//DTD HTML 3.2//EN", "html32/html32.dtd");
    DTD_BUILTIN.put("-//W3C//DTD HTML 3.2 Final//EN", "html32/html32.dtd");

    // HTML 4.0
    DTD_BUILTIN.put("-//W3C//DTD HTML 4.0//EN", "html4/strict.dtd");
    DTD_BUILTIN.put("-//W3C//DTD HTML 4.0 Transitional//EN", "html4/loose.dtd");
    DTD_BUILTIN.put("-//W3C//DTD HTML 4.0 Frameset//EN", "html4/frameset.dtd");

    // HTML 4.01
    DTD_BUILTIN.put("-//W3C//DTD HTML 4.01//EN", "html4/strict.dtd");
    DTD_BUILTIN.put("-//W3C//DTD HTML 4.01 Transitional//EN", "html4/loose.dtd");
    DTD_BUILTIN.put("-//W3C//DTD HTML 4.01 Frameset//EN", "html4/frameset.dtd");

    // XHTML 1.0
    DTD_BUILTIN.put("-//W3C//DTD XHTML 1.0 Strict//EN", "xhtml1/xhtml1-strict.dtd");
    DTD_BUILTIN.put("-//W3C//DTD XHTML 1.0 Transitional//EN", "xhtml1/xhtml1-transitional.dtd");
    DTD_BUILTIN.put("-//W3C//DTD XHTML 1.0 Frameset//EN", "xhtml1/xhtml1-frameset.dtd");

    // XHTML 1.1
    DTD_BUILTIN.put("//W3C//DTD XHTML 1.1//EN", "xhtml1/xhtml11.dtd");
  }

  private static final String[] SCHEMA_FOLDERS = new String[] {"xhtml1", "jsf"};

  private static LSInput createLSInput(InputStream inputStream) {
    if (inputStream != null) {
      try {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementation impl = registry.getDOMImplementation("XML 1.0 LS 3.0");
        DOMImplementationLS implls = (DOMImplementationLS) impl;
        LSInput lsInput = implls.createLSInput();
        lsInput.setByteStream(inputStream);
        return lsInput;
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
    return null;
  }

  /**
   * Gets a built-in DTD.
   */
  private static InputStream getBuiltinDTD(String publicId) {
    String fileName = DTD_BUILTIN.get(publicId);
    if (fileName != null) {
      return getBuiltinDTDByFileName(fileName);
    }
    return null;
  }

  /**
   * Gets a built-in DTD by filename.
   */
  private static InputStream getBuiltinDTDByFileName(String fileName) {
    return SchemaResolver.class.getResourceAsStream("/org/sonar/plugins/xml/dtd/" + fileName);
  }

  /**
   * Get a built-in XML schema.
   */
  @CheckForNull
  public static InputStream getBuiltinSchema(String systemId) {
    InputStream input;

    // try as namespace
    input = getBuiltinSchemaByNamespace(systemId);
    if (input != null) {
      return input;
    }
    
    // try as built-in resource
    input = getBuiltinSchemaByFileName(systemId);
    if (input != null) {
      return input;
    }

    // try as url resource
    input = getSchemaByURL(systemId);
    if (input != null) {
      return input;
    }

    // try as file system resource

    try {
      return new FileInputStream(systemId);
    } catch (FileNotFoundException e) {
      LOG.warn("Could not find schema " + systemId);
      return null;
    }
  }

  /**
   * Gets a XML schema by URL.
   */
  private static InputStream getSchemaByURL(String urlStr) {
    URL url;
    try {
      url = new URL(urlStr);
    } catch (MalformedURLException e) {
      return null;
    }

    try {
      return url.openStream();
    } catch (IOException e) {
      LOG.warn("Unable to get {}", urlStr, e);
      return null;
    }
  }

  /**
   * Gets a built-in XML schema by filename.
   */
  private static InputStream getBuiltinSchemaByFileName(String fileName) {
    InputStream input = null;
    try {
      input = SchemaResolver.class.getResourceAsStream(fileName);
    } catch (Exception e) {
      LOG.warn("Error while trying to read {}", fileName, e);
    }
    if (input == null) {
      for (String folder : SCHEMA_FOLDERS) {
        input = SchemaResolver.class.getResourceAsStream(folder + "/" + fileName);
        if (input != null) {
          break;
        }
      }
    }
    return input;
  }

  /**
   * Gets a built-in XML schema by nameSpace.
   */
  private static InputStream getBuiltinSchemaByNamespace(String nameSpace) {
    String fileName = SCHEMAS_BUILTIN.get(nameSpace);
    if (fileName != null) {
      return getBuiltinSchemaByFileName(fileName);
    }
    return null;
  }

  /**
   * ResourceResolver tries to resolve schema's and dtd's with built-in resources or external files.
   */
  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {

    LOG.debug("Trying to resolve type = {} namespace = {} publicId = {} systemId = {} baseURI = {}", new String[] {type, namespaceURI, publicId, systemId, baseURI});

    InputStream input;

    // try as DTD
    if (publicId != null && publicId.contains("//DTD")) {
      input = getBuiltinDTD(publicId);
    } else {

      // try as namespace
      input = getBuiltinSchemaByNamespace(namespaceURI);

      // try as built-in XML schema
      if (input == null) {
        input = getBuiltinSchemaByFileName(systemId);
      }

      // try as built-in entity
      if (input == null) {
        if (StringUtils.startsWithIgnoreCase(systemId, "html")) {
          input = getBuiltinDTDByFileName("html4/" + systemId);
        } else {
          input = getBuiltinDTDByFileName("xhtml1/" + systemId);
        }
      }
    }

    if (input == null) {
      LOG.debug("Could not resolve resource: " + systemId);
      return null;
    } else {
      return createLSInput(input);
    }
  }
}
