package com.sngular.kloadgen.processor.objectcreatorfactory;

import java.io.IOException;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.processor.objectcreatorfactory.impl.AvroObjectCreatorFactory;
import com.sngular.kloadgen.processor.objectcreatorfactory.impl.JsonObjectCreatorFactory;
import com.sngular.kloadgen.processor.objectcreatorfactory.impl.ProtobufObjectCreatorFactory;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectCreatorFactoryHelper {

  private ObjectCreatorFactoryHelper() {}

  public static ObjectCreatorFactory getInstance(final SchemaTypeEnum schemaType, final Object schema, final BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata) {
    final ObjectCreatorFactory objectCreatorFactory;
    try {
      objectCreatorFactory = switch (schemaType) {
        case JSON -> new JsonObjectCreatorFactory();
        case AVRO -> new AvroObjectCreatorFactory(schema, metadata);
        case PROTOBUF -> new ProtobufObjectCreatorFactory(schema, metadata);
        default -> throw new KLoadGenException("Unsupported schema type");
      };
    } catch (KLoadGenException | DescriptorValidationException | IOException e) {
      final String logMsg = "Please, make sure that the schema sources fed are correct";
      log.error(logMsg, e);
      throw new KLoadGenException("Error obtaining object creator factory. " + logMsg);
    }
    return objectCreatorFactory;
  }
}
