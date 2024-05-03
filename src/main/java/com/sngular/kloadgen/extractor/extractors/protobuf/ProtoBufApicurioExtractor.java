package com.sngular.kloadgen.extractor.extractors.protobuf;

import java.util.List;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import io.apicurio.registry.utils.protobuf.schema.ProtobufSchema;

public class ProtoBufApicurioExtractor extends AbstractProtoFileExtractor implements Extractor<ParsedSchema> {

  public final List<FieldValueMapping> processSchema(final ParsedSchema schemaReceived) {
    return processSchemaDefault(((ProtobufSchema) schemaReceived.schema()).getProtoFileElement());
  }

  public final List<String> getSchemaNameList(final String schema) {
    return getSchemaNameListDefault(schema);
  }

}