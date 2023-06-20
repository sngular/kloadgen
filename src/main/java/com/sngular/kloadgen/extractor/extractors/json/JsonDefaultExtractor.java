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

public class JsonDefaultExtractor extends AbstractJsonExtractor implements Extractor<org.everit.json.schema.Schema> {

    @Override
    public List<FieldValueMapping> processSchema(org.everit.json.schema.Schema schema) {

        Schema parsed =  jsonSchemaParser.parse(schema.toString());

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
    public List<String> getSchemaNameList(String schema) {
        Schema parsed =  jsonSchemaParser.parse(schema);
        return parsed.getProperties().stream().map(Field::getName).collect(Collectors.toList());
    }
}
