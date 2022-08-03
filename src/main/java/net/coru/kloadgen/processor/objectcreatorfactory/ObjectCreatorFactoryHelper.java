package net.coru.kloadgen.processor.objectcreatorfactory;

import java.io.IOException;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.processor.objectcreatorfactory.impl.AvroObjectCreatorFactory;
import net.coru.kloadgen.processor.objectcreatorfactory.impl.JsonObjectCreatorFactory;
import net.coru.kloadgen.processor.objectcreatorfactory.impl.ProtobufObjectCreatorFactory;

@Slf4j
public class ObjectCreatorFactoryHelper {

  private ObjectCreatorFactoryHelper() {}

  public static ObjectCreatorFactory getInstance(final SchemaTypeEnum schemaType, final Object schema, final Object metadata) {
    final ObjectCreatorFactory objectCreatorFactory;
    try {
      switch (schemaType) {
        case JSON:
          objectCreatorFactory = new JsonObjectCreatorFactory();
          break;
        case AVRO:
          objectCreatorFactory = new AvroObjectCreatorFactory(schema, metadata);
          break;
        case PROTOBUF:
          objectCreatorFactory = new ProtobufObjectCreatorFactory(schema, metadata);
          break;
        default:
          throw new KLoadGenException("Unsupported schema type");
      }
    } catch (KLoadGenException | DescriptorValidationException | IOException e) {
      final String logMsg = "Please, make sure that the schema sources fed are correct";
      log.error(logMsg, e);
      throw new KLoadGenException("Error obtaining object creator factory. " + logMsg);
    }
    return objectCreatorFactory;
  }
}
