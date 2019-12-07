package net.coru.kloadgen.input.avro;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;
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

  @Test
  public void testFlatPropertiesListSimpleRecord(@Wiremock WireMockServer server) throws IOException, RestClientException {
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(
        "http://localhost:" + server.port(),
        "avrosubject"
    );

    assertThat(fieldValueMappingList).hasSize(2);
    assertThat(fieldValueMappingList).containsExactlyInAnyOrder(
        new FieldValueMapping("Name", "string"),
        new FieldValueMapping("Age", "int")
    );
  }

  @Test
  public void testFlatPropertiesListArrayRecord(@Wiremock WireMockServer server) throws IOException, RestClientException {
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(
        "http://localhost:" + server.port(),
        "users"
    );

    assertThat(fieldValueMappingList).hasSize(2);
    assertThat(fieldValueMappingList).containsExactlyInAnyOrder(
        new FieldValueMapping("Users[].id", "long"),
        new FieldValueMapping("Users[].name", "string")
    );
  }
}