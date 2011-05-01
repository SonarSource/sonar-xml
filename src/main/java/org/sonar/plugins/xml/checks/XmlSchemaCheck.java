/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
 * dev@sonar.codehaus.org
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

package org.sonar.plugins.xml.checks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.apache.xerces.impl.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.check.Cardinality;
import org.sonar.check.IsoCategory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.xml.parsers.DetectSchemaParser;
import org.sonar.plugins.xml.schemas.SchemaResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * Perform schema check using xerces parser.
 * 
 * @author Matthijs Galesloot
 * @since 1.0
 */
@Rule(key = "XmlSchemaCheck", name = "XML Schema Check", description = "XML Schema Check", priority = Priority.CRITICAL,
    cardinality = Cardinality.MULTIPLE)
public class XmlSchemaCheck extends AbstractPageCheck {

  /**
   * MessageHandler creates violations for errors and warnings. The handler is assigned to {@link Validator} to catch the errors and
   * warnings raised by the validator.
   */
  private class MessageHandler implements ErrorHandler {

    private void createViolation(SAXParseException e) {
      XmlSchemaCheck.this.createViolation(e.getLineNumber(), e.getLocalizedMessage());
    }

    public void error(SAXParseException e) throws SAXException {
      createViolation(e);
    }

    public void fatalError(SAXParseException e) throws SAXException {
      createViolation(e);
    }

    public void warning(SAXParseException e) throws SAXException {
      createViolation(e);
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(XmlSchemaCheck.class);

  /**
   * Create xsd schema for a list of schema's.
   */
  private static Schema createSchema(String validationSchemas) {

    List<Source> schemaSources = new ArrayList<Source>();
    String[] schemaList = StringUtils.split(validationSchemas, " \t\n");

    // load each schema in a StreamSource.
    for (String schemaReference : schemaList) {
      InputStream input = SchemaResolver.getBuiltinSchemaByNamespace(schemaReference);
      if (input != null) {
        schemaSources.add(new StreamSource(input));
      } else {
        try {
          schemaSources.add(new StreamSource(new FileInputStream(schemaReference)));
        } catch (FileNotFoundException e) {
          LOG.error("Could not find schema " + schemaReference);
          throw new SonarException(e);
        }
      }
    }

    // create a schema for the list of StreamSources.
    try {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(new SchemaResolver());

      return schemaFactory.newSchema(schemaSources.toArray(new Source[schemaSources.size()]));
    } catch (SAXException e) {
      throw new SonarException(e);
    }
  }

  /**
   * filePattern indicates which files should be checked.
   */
  @RuleProperty(key = "filePattern", description = "filePattern")
  private String filePattern;

  /**
   * schemas may refer to a schema that is provided as a built-in resource, a web resource or a file resource.
   */
  @RuleProperty(key = "schemas", description = "Schemas")
  private String schemas;

  /**
   * Checks if a certain message has already been raised. Avoids duplicate messages.
   */
  private boolean containsMessage(SAXException e) {
    if (e instanceof SAXParseException) {
      SAXParseException spe = (SAXParseException) e;
      for (Violation v : getWebSourceCode().getViolations()) {
        if (v.getLineId().equals(spe.getLineNumber()) && v.getMessage().equals(spe.getMessage())) {
          return true;
        }
      }
    }
    return false;
  }

  private String detectSchema() {
    return new DetectSchemaParser().findSchemaOrDTD(getWebSourceCode().createInputStream());
  }

  public String getFilePattern() {
    return filePattern;
  }

  public String getSchemas() {
    return schemas;
  }

  private void setFeature(Validator validator, String feature, boolean value) {
    try {
      validator.setFeature(feature, value);
    } catch (SAXNotRecognizedException e) {
      throw new SonarException(e);
    } catch (SAXNotSupportedException e) {
      throw new SonarException(e);
    }
  }

  public void setFilePattern(String filePattern) {
    this.filePattern = filePattern;
  }

  public void setSchemas(String schemas) {
    this.schemas = schemas;
  }

  private void validate() {
    if ("autodetect".equalsIgnoreCase(schemas)) {
      String validationSchema = detectSchema();
      if (validationSchema != null) {
        validate(validationSchema);
      }
    } else {
      validate(schemas);
    }
  }

  private void validate(String validationSchemas) {

    // Create a new validator
    Validator validator = createSchema(validationSchemas).newValidator();
    setFeature(validator, Constants.XERCES_FEATURE_PREFIX + "continue-after-fatal-error", true);
    validator.setErrorHandler(new MessageHandler());
    validator.setResourceResolver(new SchemaResolver());

    // Validate and catch the exceptions. MessageHandler will receive the errors and warnings.
    try {
      validator.validate(new StreamSource(getWebSourceCode().createInputStream()));
    } catch (SAXException e) {
      if ( !containsMessage(e)) {
        createViolation(0, e.getMessage());
      }
    } catch (IOException e) {
      throw new SonarException(e);
    }
  }

  @Override
  public void validate(XmlSourceCode xmlSourceCode) {
    setWebSourceCode(xmlSourceCode);

    if (schemas != null && isFileIncluded(filePattern)) {
      validate();
    }
  }
}
