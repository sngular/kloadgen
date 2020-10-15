package net.coru.kloadgen.processor;

import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG;
import static java.util.Arrays.asList;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.SchemaBuilder;
import org.apache.groovy.util.Maps;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

@ExtendWith({
    WiremockResolver.class,
    WiremockUriResolver.class
})
class AvroSchemaProcessorTest {

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
  void textAvroSchemaProcessor(@Wiremock WireMockServer server) throws KLoadGenException {
    List<FieldValueMapping> fieldValueMappingList = asList(
        new FieldValueMapping("name", "string", 0, "Jose"),
        new FieldValueMapping("age", "int", 0, "43"));

    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_AUTH_FLAG, FLAG_YES);
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_AUTH_KEY, SCHEMA_REGISTRY_AUTH_BASIC_TYPE);
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
    JMeterContextService.getContext().getProperties().put(USER_INFO_CONFIG, "foo:foo");

    AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaBuilder.builder().record("testing").fields().requiredString("name").optionalInt("age").endRecord(),
        new SchemaMetadata(1, 1, ""), fieldValueMappingList);
    EnrichedRecord message = avroSchemaProcessor.next();
    assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    assertThat(message.getGenericRecord()).isNotNull();
    assertThat(message.getGenericRecord()).hasFieldOrPropertyWithValue("values", asList("Jose", 43).toArray());

  }

  @Test
  void textAvroSchemaProcessorArrayMap(@Wiremock WireMockServer server) throws KLoadGenException {
    List<FieldValueMapping> fieldValueMappingList = Collections.singletonList(
        new FieldValueMapping("values[2]", "string-map-array", 2, "n:1, t:2"));

    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_AUTH_FLAG, FLAG_YES);
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_AUTH_KEY, SCHEMA_REGISTRY_AUTH_BASIC_TYPE);
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
    JMeterContextService.getContext().getProperties().put(USER_INFO_CONFIG, "foo:foo");

    AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor();
    avroSchemaProcessor.processSchema(SchemaBuilder
        .builder()
        .record("arrayMap")
        .fields()
        .name("values")
        .type()
        .array()
        .items()
        .type(SchemaBuilder
            .builder()
            .map()
            .values()
            .stringType()
            .getValueType())
        .noDefault()
        .endRecord(),
        new SchemaMetadata(1, 1, ""),
        fieldValueMappingList);

    EnrichedRecord message = avroSchemaProcessor.next();
    assertThat(message).isNotNull().isInstanceOf(EnrichedRecord.class);
    assertThat(message.getGenericRecord()).isNotNull();
    assertThat(message.getGenericRecord().get("values")).isInstanceOf(List.class);
    List<Map<String, Object>> result = (List<Map<String, Object>>) message.getGenericRecord().get("values");
    assertThat(result).hasSize(2).contains(Maps.of("n","1","t","2"), Maps.of("n","1","t","2"));
  }
}