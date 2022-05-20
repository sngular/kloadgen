/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.extractor;

import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
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
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

@ExtendWith({
    WiremockResolver.class,
    WiremockUriResolver.class
})
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
  @DisplayName("Should extract simple Record")
  void testFlatPropertiesListSimpleRecord(@Wiremock WireMockServer server) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Pair<String, List<FieldValueMapping>> fieldValueMappingList = schemaExtractor.flatPropertiesList("avroSubject");

    assertThat(fieldValueMappingList.getRight())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("Name").fieldType("string").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("Age").fieldType("int").required(true).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should extract Array of Record")
  void testFlatPropertiesListArrayRecord(@Wiremock WireMockServer server) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Pair<String, List<FieldValueMapping>> fieldValueMappingList = schemaExtractor.flatPropertiesList("users");

    assertThat(fieldValueMappingList.getRight())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("Users[].id").fieldType("long").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("Users[].name").fieldType("string").required(true).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should extract Map of Record")
  void testFlatPropertiesListMapArray(@Wiremock WireMockServer server) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Pair<String, List<FieldValueMapping>> fieldValueMappingList = schemaExtractor.flatPropertiesList("arrayMap");

    assertThat(fieldValueMappingList.getRight())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("name").fieldType("string").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("values[][:]").fieldType("string-map-array").required(true).isAncestorRequired(true).build()
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
            FieldValueMapping.builder().fieldName("fieldMySchema.testInt_id").fieldType("int").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("fieldMySchema.testLong").fieldType("long").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("fieldMySchema.fieldString").fieldType("string").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("timestamp").fieldType("long").required(true).isAncestorRequired(true).build()
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
            FieldValueMapping.builder().fieldName("mapOfString[:]").fieldType("string-map").required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("arrayOfString[]").fieldType("string-array").required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("arrayOfMap[][:]").fieldType("string-map-array").required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("mapOfArray[:][]").fieldType("int-array-map").required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("mapOfArrayOfRecord[:][].name").fieldType("string").required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("mapOfArrayOfRecord[:][].age").fieldType("int").required(true).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("arrayOfMapOfRecord[][:].name").fieldType("string").required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("arrayOfMapOfRecord[][:].age").fieldType("int").required(true).isAncestorRequired(false).build()
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
            FieldValueMapping.builder().fieldName("theMap[:][].otherType.addTypeId").fieldType("string").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].otherType.name").fieldType("string").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].otherType.otherField").fieldType("string").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].addAmount").fieldType("bytes_decimal").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].addCode").fieldType("string").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].metadataMap[:]").fieldType("string-map").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].metadataArray[]").fieldType("string-array").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].metadataMapMap[:][:]").fieldType("string-map-map").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("theMap[:][].metadataArrayArray[][]").fieldType("string-array-array").required(true).isAncestorRequired(true).build()
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
            FieldValueMapping.builder().fieldName("Date").fieldType("int_date").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("TimeMillis").fieldType("int_time-millis").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("TimeMicros").fieldType("long_time-micros").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("TimestampMillis").fieldType("long_timestamp-millis").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("TimestampMicros").fieldType("long_timestamp-micros").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("LocalTimestampMillis").fieldType("long_local-timestamp-millis").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("LocalTimestampMicros").fieldType("long_local-timestamp-micros").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("UUID").fieldType("string_uuid").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("Decimal").fieldType("bytes_decimal").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("DecimalFixed").fieldType("fixed_decimal").required(true).isAncestorRequired(true).build()
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
            FieldValueMapping.builder().fieldName("mainObject.arrayValue[].optional1").fieldType("string").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mainObject.arrayValue[].optional2").fieldType("string").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mainObject.arrayValue[].optional3").fieldType("string").required(false).isAncestorRequired(true).build()
        );
  }
}