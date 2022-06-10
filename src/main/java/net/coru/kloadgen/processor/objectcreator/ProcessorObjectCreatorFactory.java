package net.coru.kloadgen.processor.objectcreator;

import java.io.IOException;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.processor.objectcreator.impl.AvroObjectCreator;
import net.coru.kloadgen.processor.objectcreator.impl.JsonObjectCreator;
import net.coru.kloadgen.processor.objectcreator.impl.ProtobufObjectCreator;

@Slf4j
public class ProcessorObjectCreatorFactory {

  public static ProcessorObjectCreator getInstance(SchemaTypeEnum schemaType, Object schema, SchemaMetadata metadata) {
    ProcessorObjectCreator objectCreator = null;
    try {
      switch (schemaType) {
        case JSON:
          objectCreator = new JsonObjectCreator();
          break;
        case AVRO:
          objectCreator = new AvroObjectCreator(schema, metadata);
          break;
        case PROTOBUF:
          objectCreator = new ProtobufObjectCreator(schema, metadata);
          break;
        default:
          throw new KLoadGenException("Unsupported schema type");
      }
    } catch(DescriptorValidationException | IOException | KLoadGenException e) {
      log.error("Please, make sure that the schema sources fed are correct", e);
    }
    return objectCreator;
  }
}
