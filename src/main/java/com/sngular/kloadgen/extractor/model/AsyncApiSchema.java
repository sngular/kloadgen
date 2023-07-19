package com.sngular.kloadgen.extractor.model;

import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AsyncApiSchema {

  String topicName;

  boolean key;

  String keyType;

  String schemaLookupStrategy;

  List<FieldValueMapping> model;
}
