package net.coru.kloadgen.processor.objectcreatorfactory;

import java.io.IOException;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.processor.objectcreatorfactory.impl.AvroObjectCreator;
import net.coru.kloadgen.processor.objectcreatorfactory.impl.JsonObjectCreator;
import net.coru.kloadgen.processor.objectcreatorfactory.impl.ProtobufObjectCreator;

@Slf4j
public class ObjectCreatorFactory {

  private ObjectCreatorFactory() {}

  public static ObjectCreator getInstance(final SchemaTypeEnum schemaType, final Object schema, final Object metadata) {
    final ObjectCreator objectCreator;
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
    } catch (KLoadGenException | DescriptorValidationException | IOException e) {
      final String logMsg = "Please, make sure that the schema sources fed are correct";
      log.error(logMsg, e);
      throw new KLoadGenException("Error obtaining object creator factory. " + logMsg);
    }
    return objectCreator;
  }
}
