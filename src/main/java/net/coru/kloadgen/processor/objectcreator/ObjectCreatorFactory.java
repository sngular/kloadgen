package net.coru.kloadgen.processor.objectcreator;

import java.io.IOException;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.processor.objectcreator.impl.AvroObjectCreatorFactory;
import net.coru.kloadgen.processor.objectcreator.impl.JsonObjectCreatorFactory;
import net.coru.kloadgen.processor.objectcreator.impl.ProtobufObjectCreatorFactory;

@Slf4j
public class ObjectCreatorFactory {

  public static ObjectCreator getInstance(SchemaTypeEnum schemaType, Object schema, Object metadata) {
    ObjectCreator objectCreator = null;
    try {
      switch (schemaType) {
        case JSON:
          objectCreator = new JsonObjectCreatorFactory();
          break;
        case AVRO:
          objectCreator = new AvroObjectCreatorFactory(schema, metadata);
          break;
        case PROTOBUF:
          objectCreator = new ProtobufObjectCreatorFactory(schema, metadata);
          break;
        default:
          throw new KLoadGenException("Unsupported schema type");
      }
    } catch(KLoadGenException | DescriptorValidationException | IOException e) {
      log.error("Please, make sure that the schema sources fed are correct", e);
    }
    return objectCreator;
  }
}
