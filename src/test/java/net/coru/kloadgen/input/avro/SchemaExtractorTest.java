package net.coru.kloadgen.input.avro;

import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_URL;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_USERNAME_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import net.coru.kloadgen.model.FieldValueMapping;
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
class SchemaExtractorTest {

  private SchemaExtractor schemaExtractor = new SchemaExtractor();

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
  public void testFlatPropertiesListSimpleRecord(@Wiremock WireMockServer server) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(
        "avroSubject"
    );

    assertThat(fieldValueMappingList).hasSize(2);
    assertThat(fieldValueMappingList).containsExactlyInAnyOrder(
        new FieldValueMapping("Name", "string"),
        new FieldValueMapping("Age", "int")
    );
  }

  @Test
  public void testFlatPropertiesListArrayRecord(@Wiremock WireMockServer server) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(
        "users"
    );

    assertThat(fieldValueMappingList).hasSize(2);
    assertThat(fieldValueMappingList).containsExactlyInAnyOrder(
        new FieldValueMapping("Users[].id", "long"),
        new FieldValueMapping("Users[].name", "string")
    );
  }
}