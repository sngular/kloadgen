package com.sngular.kloadgen.extractor.extractors.protobuff;

import java.util.List;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;

public class ProtoBufApicurioExtractor extends AbstractProtoFileExtractor implements Extractor<ProtoFileElement> {

  public final List<FieldValueMapping> processSchema(final ProtoFileElement schemaReceived) {
    return processSchemaDefault(schemaReceived);
  }

  public final List<String> getSchemaNameList(final String schema) {
    return getSchemaNameListDefault(schema);
  }

}