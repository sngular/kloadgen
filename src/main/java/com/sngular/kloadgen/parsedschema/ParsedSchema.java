package com.sngular.kloadgen.parsedschema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import org.apache.avro.Schema;

public class ParsedSchema<T> implements IParsedSchema<T> {

  private final T schema;
  private final String schemaType;

  public ParsedSchema(final String fileContent, final String schemaType) {
    switch (schemaType) {
      case "AVRO" -> {
        this.schema = (T) new Schema.Parser().parse(fileContent);
      }
      case "JSON" -> {
        this.schema = (T) new JsonSchema(fileContent);
      }
      case "PROTOBUF" -> {
        this.schema = (T) new ProtobufSchema(fileContent);
      }
      default -> throw new IllegalArgumentException("schemaType not supported: " + schemaType);
    }
    this.schemaType = schemaType;
  }

  public ParsedSchema(final File file, final String schemaType) throws IOException {
    this(new String(Files.readAllBytes(file.toPath())), schemaType);
  }

  public ParsedSchema(final T schema, final String type) {
    this.schema = schema;
    this.schemaType = type;
  }

  public ParsedSchema(final T schema) {
    this.schema = schema;
    this.schemaType = switch (this.schema.getClass().getSimpleName()) {
      case "Schema", "AvroSchema", "UnionSchema", "RecordSchema" -> "AVRO";
      case "ProtoBuf", "ProtobufSchema", "ProtoFileElement" -> "PROTOBUF";
      case "JsonSchema", "ObjectSchema" -> "JSON";
      default -> throw new KLoadGenException(String.format("Need to specify schemaType for %s", this.schema.getClass().getSimpleName()));
    };
  }

  public ParsedSchema(final io.confluent.kafka.schemaregistry.ParsedSchema parsedSchema) throws IOException {
    this(parsedSchema.canonicalString(), parsedSchema.schemaType());
  }

  public final Object schema() {
    return this.schema;
  }

  public final String schemaType() {
    return this.schemaType;
  }

  public final String name() {
    return null;
  }

  public final String canonicalString() {
    return switch (this.schemaType) {
      case "AVRO" -> stringAvro(this.schema);
      case "JSON" -> stringJson(this.schema);
      case "PROTOBUF" -> stringProto(this.schema);
      default -> this.schema.toString();
    };
  }

  private String stringAvro(final T schema) {
    final String result;
    if (schema instanceof AvroSchema) {
      result = ((AvroSchema) this.schema).canonicalString();
    } else {
      result = this.schema.toString();
    }
    return result;
  }

  private String stringJson(final T schema) {
    final String result;
    if (schema instanceof JsonSchema) {
      result = ((JsonSchema) this.schema).canonicalString();
    } else {
      result = this.schema.toString();
    }
    return result;
  }

  private String stringProto(final T schema) {
    final String result;
    if (schema instanceof ProtobufSchema) {
      result = ((ProtobufSchema) this.schema).canonicalString();
    } else if (schema instanceof ProtoFileElement) {
      result = ((ProtoFileElement) this.schema).toSchema();
    } else {
      result = this.schema.toString();
    }
    return result;
  }

  public final Object rawSchema() {
    return switch (this.schemaType) {
      case "AVRO" -> rawAvro(this.schema);
      case "JSON" -> rawJson(this.schema);
      case "PROTOBUF" -> rawProto(this.schema);
      default -> this.schema;
    };
  }

  private Object rawAvro(final T schema) {
    final Object result;
    if (schema instanceof AvroSchema) {
      result = ((AvroSchema) this.schema).rawSchema();
    } else {
      result = this.schema;
    }
    return result;
  }

  private Object rawJson(final T schema) {
    final Object result;
    if (schema instanceof JsonSchema) {
      result = ((JsonSchema) this.schema).rawSchema();
    } else {
      result = this.schema.toString();
    }
    return result;
  }

  private Object rawProto(final T schema) {
    final Object result;
    if (schema instanceof ProtobufSchema) {
      result = ((ProtobufSchema) this.schema).rawSchema();
    } else {
      result = this.schema;
    }
    return result;
  }

}
