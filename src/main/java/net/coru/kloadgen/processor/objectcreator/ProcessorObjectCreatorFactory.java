package net.coru.kloadgen.processor.objectcreator;

import java.io.IOException;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.processor.objectcreator.impl.AvroObjectCreator;
import net.coru.kloadgen.processor.objectcreator.impl.JsonObjectCreator;
import net.coru.kloadgen.processor.objectcreator.impl.ProtobufObjectCreator;

@Slf4j
public class ProcessorObjectCreatorFactory {

  public static ProcessorObjectCreator getInstance(SchemaTypeEnum schemaType, Object schema, SchemaMetadata metadata) {
    try {
      switch (schemaType) {
        case JSON:
          return new JsonObjectCreator();
        case AVRO:
          return new AvroObjectCreator(schema, metadata);
        case PROTOBUF:
          return new ProtobufObjectCreator(schema, metadata);
        default:
          return null;
      }
    } catch(DescriptorValidationException | IOException e) {
      log.error("Please, make sure that the schema sources fed are correct", e);
      return null;
    }
  }
}
