package net.coru.kloadgen.input;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import java.io.IOException;
import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;
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

  @Test
  public void textAvroSchemaProcessor(@Wiremock WireMockServer server) throws IOException, RestClientException {
    List<FieldValueMapping> fieldValueMappingList = asList(
        new FieldValueMapping("Name", "string", 0, "Jose"),
        new FieldValueMapping("Age", "int", 0, "43"));

    AvroSchemaProcessor avroSchemaProcessor = new AvroSchemaProcessor("http://localhost:" + server.port(), "avrosubject", fieldValueMappingList, "foo", "foo");

    Object message = avroSchemaProcessor.next();
    assertThat(message).isNotNull();
    assertThat(message).isInstanceOf(EnrichedRecord.class);

    EnrichedRecord enrichedRecord = (EnrichedRecord) message;
    assertThat(enrichedRecord.getGenericRecord()).isNotNull();
    assertThat(enrichedRecord.getGenericRecord()).hasFieldOrPropertyWithValue("values", asList("Jose", 43).toArray());

  }

}