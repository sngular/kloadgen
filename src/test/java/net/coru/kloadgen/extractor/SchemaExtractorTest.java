/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.extractor;

import static net.coru.kloadgen.model.ConstraintTypeEnum.MAXIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MINIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.REGEX;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.testutil.FileHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@WireMockTest
class SchemaExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final SchemaExtractor schemaExtractor = new SchemaExtractorImpl();

  @BeforeEach
  public void setUp() {
    File file = new File("src/test/resources");
    String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  @DisplayName("Should configure Schema Extractor Properties")
  void testFlatPropertiesListSimpleRecord(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, RestClientException {

    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, wmRuntimeInfo.getHttpBaseUrl());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Pair<String, List<FieldValueMapping>> fieldValueMappingList = schemaExtractor.flatPropertiesList(
        "avroSubject"
    );

    assertThat(fieldValueMappingList.getRight())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("Name", "string"),
            new FieldValueMapping("Age", "int")
        );
  }

  @Test
  @DisplayName("Should extract Array of Record")
  void testFlatPropertiesListArrayRecord(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, wmRuntimeInfo.getHttpBaseUrl());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Pair<String, List<FieldValueMapping>> fieldValueMappingList = schemaExtractor.flatPropertiesList("users");

    assertThat(fieldValueMappingList.getRight())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("Users[].id", "long", 0, ""),
            new FieldValueMapping("Users[].name", "string", 0, "")
        );
  }

  @Test
  @DisplayName("Should extract Map of Record")
  void testFlatPropertiesListMapArray(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, wmRuntimeInfo.getHttpBaseUrl());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Pair<String, List<FieldValueMapping>> fieldValueMappingList = schemaExtractor.flatPropertiesList("arrayMap");

    assertThat(fieldValueMappingList.getRight())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("name", "string", 0, "", true, true),
            new FieldValueMapping("values[][:]", "string-map-array", 0, "", true, true)
        );
  }

  @Test
  @DisplayName("Should extract Embedded Record")
  void testFlatPropertiesEmbeddedAvros() throws IOException {
    File testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));
    assertThat(fieldValueMappingList)
        .hasSize(4)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("fieldMySchema.testInt_id", "int",0,""),
            new FieldValueMapping("fieldMySchema.testLong", "long",0,""),
            new FieldValueMapping("fieldMySchema.fieldString", "string",0,""),
            new FieldValueMapping("timestamp", "long",0,"", true, true)
        );
  }

  @Test
  @DisplayName("Should extract Optional Map with Array/Record")
  void testFlatPropertiesOptionalMapArray() throws IOException {

    File testFile = fileHelper.getFile("/avro-files/testOptionalMap.avsc");

    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));

    assertThat(fieldValueMappingList)
        .hasSize(8)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("mapOfString[:]", "string-map", 0, "", false, false),
            new FieldValueMapping("arrayOfString[]", "string-array", 0, "", false, false),
            new FieldValueMapping("arrayOfMap[][:]", "string-map-array", 0, "", false, false),
            new FieldValueMapping("mapOfArray[:][]", "int-array-map", 0, "", false, false),
            new FieldValueMapping("mapOfArrayOfRecord[:][].name", "string", 0, "", false, false),
            new FieldValueMapping("mapOfArrayOfRecord[:][].age", "int", 0, "", true, false),
            new FieldValueMapping("arrayOfMapOfRecord[][:].name", "string", 0, "", false, false),
            new FieldValueMapping("arrayOfMapOfRecord[][:].age", "int", 0, "", true, false)
        );
  }

  @Test
  @DisplayName("Should extract Map of Record")
  void testFlatPropertiesMap() throws IOException {
    File testFile = fileHelper.getFile("/avro-files/testMap.avsc");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));
    assertThat(fieldValueMappingList)
        .hasSize(9)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("theMap[:][].otherType.addTypeId", "string", 0, "", true, true),
            new FieldValueMapping("theMap[:][].otherType.name", "string", 0, "", true, true),
            new FieldValueMapping("theMap[:][].otherType.otherField", "string", 0, "", false, true),
            new FieldValueMapping("theMap[:][].addAmount", "bytes_decimal", 0, "", true, true),
            new FieldValueMapping("theMap[:][].addCode", "string", 0, "", false, true),
            new FieldValueMapping("theMap[:][].metadataMap[:]", "string-map", 0, "", false, true),
            new FieldValueMapping("theMap[:][].metadataArray[]", "string-array", 0, "", false, true),
            new FieldValueMapping("theMap[:][].metadataMapMap[:][:]", "string-map-map", 0, "", true, true),
            new FieldValueMapping("theMap[:][].metadataArrayArray[][]", "string-array-array", 0, "", true, true)
        );
  }

  @Test
  @DisplayName("Should extract Logical times")
  void testFlatPropertiesLogicalTypes() throws IOException {

    File testFile = fileHelper.getFile("/avro-files/testLogicalTypes.avsc");

    List<FieldValueMapping> fieldValueMappingList =
        schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));

    assertThat(fieldValueMappingList)
        .hasSize(10)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("Date", "int_date"),
          new FieldValueMapping("TimeMillis", "int_time-millis"),
          new FieldValueMapping("TimeMicros", "long_time-micros"),
          new FieldValueMapping("TimestampMillis", "long_timestamp-millis"),
          new FieldValueMapping("TimestampMicros", "long_timestamp-micros"),
          new FieldValueMapping("LocalTimestampMillis", "long_local-timestamp-millis"),
          new FieldValueMapping("LocalTimestampMicros", "long_local-timestamp-micros"),
          new FieldValueMapping("UUID","string_uuid"),
          new FieldValueMapping("Decimal","bytes_decimal"),
          new FieldValueMapping("DecimalFixed","fixed_decimal")
      );
  }

  @Test
  @DisplayName("Should extract Optional Array")
  void testFlatPropertiesOptionalArray() throws IOException {

    File testFile = fileHelper.getFile("/avro-files/issue.avsc");

    List<FieldValueMapping> fieldValueMappingList =
        schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));

    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("mainObject.arrayValue[].optional1", "string", 0, "", false, true),
            new FieldValueMapping("mainObject.arrayValue[].optional2", "string", 0, "", false, true),
            new FieldValueMapping("mainObject.arrayValue[].optional3", "string", 0, "", false, true)
        );
  }

  @Test
  @DisplayName("Should propagate required status to children fields not required of a required field")
  void testRequiredPropagationChildrenFields() throws IOException {
    File testFile = fileHelper.getFile("/jsonschema/complex-document.jcs");

    List<FieldValueMapping> fieldValueMappingList =
            schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));

    assertThat(fieldValueMappingList)
            .contains(
                    new FieldValueMapping("geopoliticalSubdivisions.level1.code", "string", 0,"", new HashMap<ConstraintTypeEnum, String>() {{
                      put(MINIMUM_VALUE, "2");
                      put(MAXIMUM_VALUE, "3");
                    }}, false,true),
                    new FieldValueMapping("geopoliticalSubdivisions.level1.freeForm", "string", 0,"", new HashMap<ConstraintTypeEnum, String>() {{
                      put(MINIMUM_VALUE, "1");
                      put(MAXIMUM_VALUE, "256");
                    }}, false,true),
                    new FieldValueMapping("geopoliticalSubdivisions.level2.code", "string", 0,"", new HashMap<ConstraintTypeEnum, String>() {{
                      put(MINIMUM_VALUE, "2");
                      put(MAXIMUM_VALUE, "3");
                    }}, false, false),
                    new FieldValueMapping("geopoliticalSubdivisions.level2.freeForm", "string", 0,"", new HashMap<ConstraintTypeEnum, String>() {{
                      put(MINIMUM_VALUE, "1");
                      put(MAXIMUM_VALUE, "256");
                    }}, false,false)
            );
  }

  @Test
  @DisplayName("Should extract fields in definitions in Json Schema")
  void testShouldExtractJsonSchemaDefinitions() throws IOException {
    File testFile = fileHelper.getFile("/jsonschema/medium-document.jcs");

    List<FieldValueMapping> fieldValueMappingList =
            schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));

    assertThat(fieldValueMappingList).contains(
            new FieldValueMapping("duty.amount.value","number", 0, "", false, false),
            new FieldValueMapping("duty.amount.currency","string", 0, "",new HashMap<ConstraintTypeEnum, String>() {{
              put(MINIMUM_VALUE, "0");
              put(MAXIMUM_VALUE, "0");
              put(REGEX, "^(.*)$");
            }}, false, false),
            new FieldValueMapping("duty.amount.exponent","number", 0, "", false, false));
  }

  @Test
  @DisplayName("Should extract maps of simple data-types from JsonSchema")
  void testShouldExtractMapSimpleDataType() throws IOException {
    File testFile = fileHelper.getFile("/jsonschema/test-map.jcs");

    List<FieldValueMapping> fieldValueMappingList =
            schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));

    assertThat(fieldValueMappingList).contains(
            new FieldValueMapping("firstName","string", 0,"", new HashMap<ConstraintTypeEnum, String>() {{
              put(MINIMUM_VALUE, "0");
              put(MAXIMUM_VALUE, "0");
            }},false, false),
            new FieldValueMapping("lastName","string", 0,"", new HashMap<ConstraintTypeEnum, String>() {{
              put(MINIMUM_VALUE, "0");
              put(MAXIMUM_VALUE, "0");
            }},true, false),
            new FieldValueMapping("age","number", 0,"",true, false),
            new FieldValueMapping("testMap.itemType[]","number-map", 0,"", false, true),
            new FieldValueMapping("testMap.itemTipo[]","string-map", 0,"", false, true)

    );
  }

}