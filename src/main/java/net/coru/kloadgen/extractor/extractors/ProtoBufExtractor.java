package net.coru.kloadgen.extractor.extractors;


import com.squareup.wire.schema.internal.parser.EnumElement;
import com.squareup.wire.schema.internal.parser.MessageElement;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.TypeElement;
import net.coru.kloadgen.model.FieldValueMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static net.coru.kloadgen.util.ProtobufHelper.*;


public class ProtoBufExtractor {

    public List<FieldValueMapping> processSchema(ProtoFileElement schema) {
        List<FieldValueMapping> attributeList = new ArrayList<>();
        schema.getTypes().forEach(field -> processField(field, attributeList));
        return attributeList;
    }

    private List<FieldValueMapping> processFieldList(TypeElement fieldList) {
        List<FieldValueMapping> completeFieldList = new ArrayList<>();
        processField(fieldList, completeFieldList);
        return completeFieldList;
    }


    private void processField(TypeElement field, List<FieldValueMapping> completeFieldList) {
        HashMap<String, TypeElement> nestedTypes = new HashMap<>();
        String fieldValueList;
        if (!field.getNestedTypes().isEmpty()) {
            field.getNestedTypes().forEach(nestedType ->
                    nestedTypes.put(nestedType.getName(), nestedType)
            );
        }
        if (field instanceof MessageElement) {
            ((MessageElement) field).getFields()
                    .forEach(
                            subfield -> {
                                if (protobufTypes.containsKey(subfield.getType())) {
                                    if ("repeated".equalsIgnoreCase(Objects.requireNonNull(subfield.getLabel()).toString())) {
                                        completeFieldList
                                                .add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]", subfield.getType().replace(subfield.getType(),
                                                        protobufTypes.get(subfield.getType())), 0, ""));
                                    } else {
                                        completeFieldList
                                                .add(new FieldValueMapping(field.getName() + "." + subfield.getName(), subfield.getType().replace(subfield.getType(),
                                                        protobufTypes.get(subfield.getType())), 0, ""));
                                    }
                                }
                                else {
                                    if (nestedTypes.containsKey(subfield.getType())) {
                                        List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(subfield.getType()));
                                        for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
                                            String[] splitText = fieldValueMapping.getFieldName().split("\\.");
                                            List<String> preparedField = Arrays.asList((Arrays.copyOfRange(splitText, 1, splitText.length)));
                                            String fieldValueMappingPrepared = String.join(".", preparedField);
                                            if ("repeated".equalsIgnoreCase(Objects.requireNonNull(subfield.getLabel()).toString())) {
                                                completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]." + fieldValueMappingPrepared, fieldValueMapping.getFieldType(), 0, ""));
                                            } else {
                                                completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "." + fieldValueMappingPrepared, fieldValueMapping.getFieldType(), 0, ""));
                                            }
                                        }
                                    }
                                }

                            }
                    );
        } else {
            EnumElement fieldEnum = (EnumElement) field;
            List<String> EnumConstantList = new ArrayList<>();
            fieldEnum.getConstants().forEach(constant -> {
                EnumConstantList.add(constant.getName());
            });
            fieldValueList = String.join(",", EnumConstantList);
            completeFieldList.add(new FieldValueMapping(fieldEnum.getName(), "enum", 0, fieldValueList));

        }
    }
}