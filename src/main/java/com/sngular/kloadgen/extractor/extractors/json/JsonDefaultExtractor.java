package com.sngular.kloadgen.extractor.extractors.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.model.json.Field;
import com.sngular.kloadgen.model.json.Schema;

public class JsonDefaultExtractor extends AbstractJsonExtractor implements Extractor<String> {

  @Override
  public final List<FieldValueMapping> processSchema(final String schema) {

    final Schema parsed = getSchemaParser().parse(schema);

    final List<FieldValueMapping> attributeList = new ArrayList<>();
    parsed.getProperties().forEach(field -> attributeList.addAll(processField(field, true, null)));

    final Set<String> requiredFields = new HashSet<>(parsed.getRequiredFields());

    for (FieldValueMapping field : attributeList) {
      if (!field.getFieldName().contains("[]") && !field.getFieldName().contains("[:]")) {
        field.setRequired(requiredFields.contains(field.getFieldName()));
      }
    }
    return attributeList;
  }

  @Override
  public final List<String> getSchemaNameList(final String schema) {
    final Schema parsed = getSchemaParser().parse(schema);
    return parsed.getProperties().stream().map(Field::getName).collect(Collectors.toList());
  }
}
