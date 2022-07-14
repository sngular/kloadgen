package net.coru.kloadgen.serializer;

import static java.util.Arrays.asList;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.SchemaProcessor;
import net.coru.kloadgen.testutil.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class ProtobufSerializerTest {

  private ProtobufSerializer protobufSerializer;

  @BeforeEach
  void setUp() {
    protobufSerializer = new ProtobufSerializer();
  }

  @Test
  void serialize() throws IOException, DescriptorValidationException {
    File testFile = new FileHelper().getFile("/proto-files/easyTest.proto");
    var fieldValueMappings = asList(
        FieldValueMapping.builder().fieldName("street").fieldType("string").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("number[]").fieldType("int-array").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("zipcode").fieldType("long").required(true).isAncestorRequired(true).build());

    ParsedSchema parsedSchema = new SchemaExtractorImpl().schemaTypesList(testFile, "Protobuf");
    SchemaProcessor protobufSchemaProcessor = new SchemaProcessor();
    protobufSchemaProcessor.processSchema(SchemaTypeEnum.PROTOBUF, parsedSchema, new SchemaMetadata(1, 1, ""), fieldValueMappings);

    var record = protobufSchemaProcessor.next();

    var message = protobufSerializer.serialize("the-topic", EnrichedRecord.builder()
                                                                          .genericRecord(((EnrichedRecord) record).getGenericRecord())
                                                                          .schemaMetadata(((EnrichedRecord) record).getSchemaMetadata())
                                                                          .build());
    assertThat(message).isNotNull();
  }
}
