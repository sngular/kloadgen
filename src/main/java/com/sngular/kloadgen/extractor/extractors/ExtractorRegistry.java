package com.sngular.kloadgen.extractor.extractors;

import java.util.List;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.model.FieldValueMapping;

public interface ExtractorRegistry<T> {
  List<FieldValueMapping> processSchema(final T schema, SchemaRegistryEnum registry);

  Object processSchema(final String fileContent);

  List<String> getSchemaNameList(final String schema, SchemaRegistryEnum registryEnum);

}
