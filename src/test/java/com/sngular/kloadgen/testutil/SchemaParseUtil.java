package com.sngular.kloadgen.testutil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import org.apache.commons.lang3.EnumUtils;

public final class SchemaParseUtil {

  private SchemaParseUtil() {
  }

  public static ParsedSchema getParsedSchema(final File schema, final String type) throws IOException {
    return getParsedSchema(new String(Files.readAllBytes(schema.toPath())), type);
  }

  public static ParsedSchema getParsedSchema(final String schema, final String type) {
    final SchemaTypeEnum schemaType = EnumUtils.isValidEnum(SchemaTypeEnum.class, type.toUpperCase())
                                          ? SchemaTypeEnum.valueOf(type.toUpperCase()) : null;
    final ParsedSchema schemaParsed;
    try {
      assert schemaType != null;
      schemaParsed = switch (schemaType) {
        case JSON -> new JsonSchema(schema);
        case AVRO -> new AvroSchema(schema);
        case PROTOBUF -> new ProtobufSchema(schema);
      };
    } catch (final KLoadGenException e) {
      final String logMsg = "Please, make sure that the schema sources fed are correct";
      throw new KLoadGenException("Error obtaining object creator factory. " + logMsg);
    }
    return schemaParsed;
  }

}
