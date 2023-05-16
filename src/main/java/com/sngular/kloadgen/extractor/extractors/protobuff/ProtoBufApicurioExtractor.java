package com.sngular.kloadgen.extractor.extractors.protobuff;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.squareup.wire.schema.internal.parser.TypeElement;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;

public class ProtoBufApicurioExtractor extends AbstractProtoFileExtractor implements Extractor<ProtobufSchema> {


  public final List<FieldValueMapping> processSchema(final ProtobufSchema schemaReceived) {
    final var schema = schemaReceived.getProtoFileElement();
    final List<FieldValueMapping> attributeList = new ArrayList<>();
    final HashMap<String, TypeElement> nestedTypes = new HashMap<>();
    schema.getTypes().forEach(field -> processField(field, attributeList, schema.getImports(), true, nestedTypes));
    return attributeList;
  }

  public final List<String> getSchemaNameList(final String schema) {
    final DynamicSchema dynamicSchema;
    try {
      dynamicSchema = DynamicSchema.parseFrom(schema.getBytes(StandardCharsets.UTF_8));
    } catch (DescriptorValidationException | IOException e) {
      throw new KLoadGenException(e);
    }
    return new ArrayList<>(dynamicSchema.getMessageTypes());
  }

}