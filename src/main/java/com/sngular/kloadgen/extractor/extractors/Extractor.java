package com.sngular.kloadgen.extractor.extractors;

import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;

public interface Extractor<T> {
  List<FieldValueMapping> processSchema(final T jsonNode);

  List<String> getSchemaNameList(final String schema);

}
