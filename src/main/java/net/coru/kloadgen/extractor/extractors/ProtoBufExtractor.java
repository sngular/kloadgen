package net.coru.kloadgen.extractor.extractors;


import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.internal.parser.EnumElement;
import com.squareup.wire.schema.internal.parser.FieldElement;
import com.squareup.wire.schema.internal.parser.MessageElement;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.TypeElement;
import net.coru.kloadgen.model.FieldValueMapping;
import org.jetbrains.annotations.NotNull;
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
        fieldNestedTypes(field, nestedTypes);
        if (field instanceof MessageElement) {
            ((MessageElement) field).getFields()
                    .forEach(
                            subfield -> {
                                Field.Label label = checkNullLabel(subfield);
                                boolean isArray = "repeated".equalsIgnoreCase(Objects.requireNonNull(label.toString()));
                                if (protobufTypes.containsKey(subfield.getType())) {
                                    if (isArray) {
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
                                    String dotType = checkDotType(subfield);
                                    if (nestedTypes.containsKey(subfield.getType())) {
                                        List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(subfield.getType()));
                                        for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
                                            if ("enum".equals(fieldValueMapping.getFieldType())) {
                                                if (isArray) {
                                                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]", fieldValueMapping.getFieldType(), 0, fieldValueMapping.getFieldValuesList().toString()));
                                                } else {
                                                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName(), fieldValueMapping.getFieldType(), 0, fieldValueMapping.getFieldValuesList().toString()));
                                                }
                                            } else {
                                                String fieldValueMappingPrepared = getFieldValueMappingPrepared(fieldValueMapping);
                                                if (isArray) {
                                                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]." + fieldValueMappingPrepared, fieldValueMapping.getFieldType(), 0, ""));
                                                } else {
                                                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "." + fieldValueMappingPrepared, fieldValueMapping.getFieldType(), 0, ""));
                                                }
                                            }
                                        }
                                    }
                                    else if (nestedTypes.containsKey(dotType)){
                                        List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(dotType));
                                        for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
                                            String fieldValueMappingPrepared = getFieldValueMappingPrepared(fieldValueMapping);
                                            if ("enum".equals(fieldValueMapping.getFieldType())) {
                                                if (isArray) {
                                                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]", fieldValueMapping.getFieldType(), 0, fieldValueMapping.getFieldValuesList().toString()));
                                                } else {
                                                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName(), fieldValueMapping.getFieldType(), 0, fieldValueMapping.getFieldValuesList().toString()));
                                                }
                                            } else {
                                                if (isArray) {
                                                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]." + fieldValueMappingPrepared, fieldValueMapping.getFieldType(), 0, ""));
                                                } else {
                                                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "." + fieldValueMappingPrepared, fieldValueMapping.getFieldType(), 0, ""));
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                    );
        } else {
            extractEnums((EnumElement) field, completeFieldList, nestedTypes);

        }
    }

    private void extractEnums(EnumElement field, List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes) {
        String fieldValueList;
        List<String> EnumConstantList = new ArrayList<>();
         {
            field.getConstants().forEach(constant -> EnumConstantList.add(constant.getName()));
            fieldValueList = String.join(",", EnumConstantList);
            if (!"".equals(fieldValueList)) {
                completeFieldList.add(new FieldValueMapping(field.getName(), "enum", 0, fieldValueList));
            }
        }
    }

    @NotNull
    private String getFieldValueMappingPrepared(FieldValueMapping fieldValueMapping) {
        String[] splitText = fieldValueMapping.getFieldName().split("\\.");
        List<String> preparedField = Arrays.asList((Arrays.copyOfRange(splitText, 1, splitText.length)));
        return String.join(".", preparedField);
    }

    private String checkDotType(FieldElement subfield) {
        String dotType = "";
        if (subfield.getType().startsWith(".")){
            String[] typeSplit = subfield.getType().split("\\.");
            dotType = typeSplit[typeSplit.length - 1];
        }
        return dotType;
    }

    private Field.Label checkNullLabel(FieldElement subfield) {
        Field.Label label;
        if (Objects.nonNull(subfield.getLabel())){
            label = subfield.getLabel();
        } else {
            label = Field.Label.OPTIONAL;
        }
        return label;
    }

    private void fieldNestedTypes(TypeElement field, HashMap<String, TypeElement> nestedTypes) {
        if (!field.getNestedTypes().isEmpty()) {
            field.getNestedTypes().forEach(nestedType ->
                    nestedTypes.put(nestedType.getName(), nestedType)
            );
        }
    }
}