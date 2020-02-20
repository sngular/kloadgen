package net.coru.kloadgen.loadgen.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import net.coru.kloadgen.exception.KLoadGenException;
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
class AvroLoadGeneratorTest {

  @Test
  public void testAvroLoadGenerator(@Wiremock WireMockServer server) throws KLoadGenException {

    List<FieldValueMapping> fieldValueMappingList = asList(
        new FieldValueMapping("Name", "string", 0, "Jose"),
        new FieldValueMapping("Age", "int", 0, "43"));

    AvroLoadGenerator avroLoadGenerator = new AvroLoadGenerator("http://localhost:" + server.port(), "avrosubject", fieldValueMappingList);

    Object message = avroLoadGenerator.nextMessage();
    assertThat(message).isNotNull();
    assertThat(message).isInstanceOf(EnrichedRecord.class);

    EnrichedRecord enrichedRecord = (EnrichedRecord) message;
    assertThat(enrichedRecord.getGenericRecord()).isNotNull();
    assertThat(enrichedRecord.getGenericRecord()).hasFieldOrPropertyWithValue("values", asList("Jose", 43).toArray());
  }
}