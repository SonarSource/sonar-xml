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
package org.sonar.plugins.xml.checks;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xerces.impl.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.xml.parsers.DetectSchemaParser;
import org.sonar.plugins.xml.parsers.DetectSchemaParser.Doctype;
import org.sonar.plugins.xml.schemas.SchemaResolver;
import org.sonar.squidbridge.annotations.RuleTemplate;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * Perform schema check using xerces parser.
 *
 * @author Matthijs Galesloot
 */
@Rule(
  key = "XmlSchemaCheck",
  name = "XML schemas should be valid",
  priority = Priority.MAJOR
)
@RuleTemplate
public class XmlSchemaCheck extends AbstractXmlCheck {

  /**
   * filePattern indicates which files should be checked.
   */
  @RuleProperty(key = "filePattern", description = "Files to be validated using Ant-style matching patterns.")
  private String filePattern;

  /**
   * schemas may refer to a schema that is provided as a built-in resource, a web resource or a file resource.
   */
  @RuleProperty(
    key = "schemas",
    description = "Whitespace-separated list of schemas to use for validation.",
    defaultValue = DEFAULT_SCHEMA,
    type = "TEXT")
  private String schemas;

  private static final Logger LOG = LoggerFactory.getLogger(XmlSchemaCheck.class);
  private static final Map<String, Schema> CACHED_SCHEMAS = new HashMap<>();
  public static final String DEFAULT_SCHEMA = "autodetect";

  /**
   * MessageHandler creates violations for errors and warnings. The handler is assigned to {@link Validator} to catch the errors and
   * warnings raised by the validator.
   */
  private class MessageHandler implements ErrorHandler {

    private void createViolation(SAXParseException e) {
      XmlSchemaCheck.this.createViolation(e.getLineNumber(), e.getLocalizedMessage());
      if (e.getLocalizedMessage().contains(UnrecoverableParseError.FAILUREMESSAGE)) {
        throw new UnrecoverableParseError(e);
      }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
      createViolation(e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      createViolation(e);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
      createViolation(e);
    }
  }

  /**
   * Exception for a parse error from which the parser cannot recover.
   */
  private static class UnrecoverableParseError extends RuntimeException {

    static final String FAILUREMESSAGE = "The reference to entity \"null\"";

    private static final long serialVersionUID = 1L;

    public UnrecoverableParseError(SAXParseException e) {
      super(e);
    }
  }

  /**
   * Create xsd schema for a list of schema's.
   */
  private static Schema createSchema(String[] schemaList) {

    final String cacheKey = StringUtils.join(schemaList, ",");
    // first try to load a cached schema.
    Schema schema = CACHED_SCHEMAS.get(cacheKey);
    if (schema != null) {
      return schema;
    }

    List<Source> schemaSources = new ArrayList<>();

    // load each schema in a StreamSource.
    for (String schemaReference : schemaList) {
      InputStream input = SchemaResolver.getBuiltinSchema(schemaReference);
      if (input == null) {
        throw new IllegalStateException("Could not load schema: " + schemaReference);
      }
      schemaSources.add(new StreamSource(input));
    }

    // create a schema for the list of StreamSources.
    try {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(new SchemaResolver());

      schema = schemaFactory.newSchema(schemaSources.toArray(new Source[schemaSources.size()]));
      CACHED_SCHEMAS.put(cacheKey, schema);
      return schema;
    } catch (SAXException e) {
      throw new IllegalStateException(e);
    }
  }

  private void autodetectSchemaAndValidate() {
    final Doctype doctype = detectSchema();

    // first try doctype as this is more specific
    if (doctype.getDtd() != null) {
      InputStream input = SchemaResolver.getBuiltinSchema(doctype.getDtd());
      if (input == null) {
        LOG.error("Could not validate " + getWebSourceCode().toString() + " for doctype " + doctype.getDtd());
      } else {
        IOUtils.closeQuietly(input);
        validate(new String[]{doctype.getDtd()});
      }
    }
    // try namespace
    else if (doctype.getNamespace() != null && !StringUtils.isEmpty(doctype.getNamespace())) {
      validate(new String[]{doctype.getNamespace()});
    } else {
      LOG.info("Could not autodetect schema for " + getWebSourceCode().toString() + ", skip validation.");
    }
  }

  /**
   * Checks if a certain message has already been raised. Avoids duplicate messages.
   */
  private boolean containsMessage(SAXException e) {
    if (e instanceof SAXParseException) {
      SAXParseException spe = (SAXParseException) e;
      for (XmlIssue i : getWebSourceCode().getXmlIssues()) {
        if (i.getLine() == spe.getLineNumber() && i.getMessage().equals(spe.getMessage())) {
          return true;
        }
      }
    }
    return false;
  }

  private Doctype detectSchema() {
    return new DetectSchemaParser().findDoctype(getWebSourceCode().createInputStream());
  }

  public String getFilePattern() {
    return filePattern;
  }

  public String getSchemas() {
    return schemas;
  }

  @VisibleForTesting
  static void setFeature(Validator validator, String feature, boolean value) {
    try {
      validator.setFeature(feature, value);
    } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
      throw new IllegalStateException(e);
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
      autodetectSchemaAndValidate();
    } else {
      String[] schemaList = StringUtils.split(schemas, " \t\n");

      validate(schemaList);
    }
  }

  private void validate(String[] schemaList) {
    // Create a new validator
    Validator validator = createSchema(schemaList).newValidator();
    setFeature(validator, Constants.XERCES_FEATURE_PREFIX + "continue-after-fatal-error", true);
    validator.setErrorHandler(new MessageHandler());
    validator.setResourceResolver(new SchemaResolver());

    // Validate and catch the exceptions. MessageHandler will receive the errors and warnings.
    try {
      LOG.info("Validate " + getWebSourceCode() + " with schema " + StringUtils.join(schemaList, ","));
      validator.validate(new StreamSource(getWebSourceCode().createInputStream()));

    } catch (SAXException e) {
      if (!containsMessage(e)) {
        createViolation(0, e.getMessage());
      }

    } catch (IOException e) {
      throw new IllegalStateException(e);

    } catch (UnrecoverableParseError e) {
      // ignore, message already reported.
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
