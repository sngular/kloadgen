/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.extractor;

import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY;
import static org.mockito.ArgumentMatchers.eq;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import net.coru.kloadgen.extractor.extractors.AvroExtractor;
import net.coru.kloadgen.extractor.extractors.JsonExtractor;
import net.coru.kloadgen.extractor.extractors.ProtoBufExtractor;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.testutil.FileHelper;
import org.apache.avro.Schema.Field;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

@ExtendWith({
    WiremockResolver.class,
    WiremockUriResolver.class
})
@RunWith(MockitoJUnitRunner.class)
class SchemaExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  @InjectMocks
  private SchemaExtractorImpl schemaExtractor;

  @Mock
  private AvroExtractor avroExtractor = Mockito.mock(AvroExtractor.class);

  @Mock
  private JsonExtractor jsonExtractor = Mockito.mock(JsonExtractor.class);

  @Mock
  private ProtoBufExtractor protoBufExtractor = Mockito.mock(ProtoBufExtractor.class);

  @BeforeEach
  public void setUp() {
    schemaExtractor = new SchemaExtractorImpl(avroExtractor, jsonExtractor, protoBufExtractor);

    File file = new File("src/test/resources");
    String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  @DisplayName("Test flatPropertiesList with AVRO")
  public void testFlatPropertiesListWithAVRO(@Wiremock WireMockServer server) throws RestClientException, IOException {

    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Mockito.doNothing().when(avroExtractor).processField(Mockito.any(Field.class), Mockito.anyList(), eq(true), eq(false));

    schemaExtractor.flatPropertiesList("avroSubject");

    Mockito.verify(avroExtractor);

  }

  @Test
  @DisplayName("Test flatPropertiesList with Json")
  public void testFlatPropertiesListWithJson(@Wiremock WireMockServer server) throws RestClientException, IOException {

    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

//    Mockito.when(jsonExtractor.processSchema(Mockito.any(JsonNode.class))).thenReturn();

    schemaExtractor.flatPropertiesList("jsonSubject");

    Mockito.verify(avroExtractor);

  }

  @Test
  @DisplayName("Test flatPropertiesList with Protobuf")
  public void testFlatPropertiesListWithProtobuf(@Wiremock WireMockServer server) throws RestClientException, IOException {

    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Mockito.doNothing().when(avroExtractor).processField(Mockito.any(Field.class), Mockito.anyList(), eq(true), eq(false));

    schemaExtractor.flatPropertiesList("avroSubject");

    Mockito.verify(avroExtractor);

  }

//  @Test
//  @DisplayName("Should capture 3+ level exception in collections. Three levels of nested collections are not allowed")
//  void testFlatPropertiesCaptureThreeLevelException() {
//    File testFile = fileHelper.getFile("/jsonschema/test-level-nested-exception.jcs");
//    assertThatExceptionOfType(KLoadGenException.class)
//        .isThrownBy(() -> {
//          List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));
//          assertThat(fieldValueMappingList).isNull();
//        })
//        .withMessage("Wrong Json Schema, 3+ consecutive nested collections are not allowed");
//  }

}