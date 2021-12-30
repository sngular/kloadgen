package net.coru.kloadgen.extractor.extractors;


import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.internal.parser.EnumElement;
import com.squareup.wire.schema.internal.parser.FieldElement;
import com.squareup.wire.schema.internal.parser.MessageElement;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.TypeElement;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.coru.kloadgen.util.ProtobufHelper.*;


public class ProtoBufExtractor {

    public static final String ARRAY_POSTFIX = "-array";
    public static final String MAP_POSTFIX = "-map";

    public List<FieldValueMapping> processSchema(ProtoFileElement schema) {
        List<FieldValueMapping> attributeList = new ArrayList<>();
        schema.getTypes().forEach(field -> processField(field, attributeList, schema.getImports()));
        return attributeList;
    }

    private List<FieldValueMapping> processFieldList(TypeElement fieldList, List<String> imports) {
        List<FieldValueMapping> completeFieldList = new ArrayList<>();
        processField(fieldList, completeFieldList, imports);
        return completeFieldList;
    }


    private void processField(TypeElement field, List<FieldValueMapping> completeFieldList, List<String> imports) {
        HashMap<String, TypeElement> nestedTypes = new HashMap<>();
        fillNestedTypes(field, nestedTypes);
        if (field instanceof MessageElement) {
            ((MessageElement) field).getFields()
                    .forEach(
                            subfield -> {
                                Field.Label label = checkNullLabel(subfield);
                                boolean isArray = "repeated".equalsIgnoreCase(Objects.requireNonNull(label.toString()));
                                boolean isMap = subfield.getType().startsWith("map");
                                if (protobufTypes.containsKey(subfield.getType())) {
                                    extractPrimitiveTypes(field, completeFieldList, subfield, isArray);
                                } else if (isMap) {
                                    extractMapType(field, completeFieldList, nestedTypes, subfield, imports);
                                } else if (!protobufTypes.containsKey(subfield.getType())) {
                                    String dotType = checkDotType(subfield.getType(), imports);
                                    if (nestedTypes.containsKey(subfield.getType())) {
                                        extractNestedTypes(field, completeFieldList, nestedTypes, subfield, isArray, imports);
                                    } else if (nestedTypes.containsKey(dotType)) {
                                        extractDotTypesWhenIsInNestedType(field, completeFieldList, nestedTypes, subfield, isArray, dotType, imports);
                                    } else {
                                        extractDotTypeWhenNotNestedType(field, completeFieldList, subfield, isArray, dotType);
                                    }
                                } else {
                                    throw new KLoadGenException("Something Odd Just Happened: Unsupported type of value");
                                }
                            }
                    );
        } else if (field instanceof EnumElement) {
            extractEnums((EnumElement) field, completeFieldList);
        } else {
            throw new KLoadGenException("Something Odd Just Happened: Unsupported type of value");
        }
    }

    private void extractDotTypeWhenNotNestedType(TypeElement field, List<FieldValueMapping> completeFieldList, FieldElement subfield, boolean isArray, String dotType) {
        if (isArray) {
            completeFieldList
                    .add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]", dotType + ARRAY_POSTFIX, 0, ""));
        } else {
            completeFieldList
                    .add(new FieldValueMapping(field.getName() + "." + subfield.getName(), dotType, 0, ""));
        }
    }

    private void extractMapType(TypeElement field, List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, List<String> imports) {
        String subFieldType = extractInternalMapFields(subfield);
        String dotTypeMap = checkDotType(subFieldType, imports);
        if (protobufTypes.containsKey(subFieldType)) {
            completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[:]", subFieldType.replace(subFieldType,
                    protobufTypes.get(subFieldType)) + MAP_POSTFIX, 0, ""));
        } else if (nestedTypes.containsKey(subFieldType)) {
            extractNestedTypesMap(field, completeFieldList, nestedTypes, subfield, imports);
        } else if (nestedTypes.containsKey(dotTypeMap)) {
            extractDotTypesMap(field, completeFieldList, nestedTypes, subfield, dotTypeMap, imports);
        } else {
            throw new KLoadGenException("Something Odd Just Happened: Unsupported type of value");
        }
    }

    @NotNull
    private String extractInternalMapFields(FieldElement subfield) {
        String[] mapSplit = subfield.getType().split(",");
        String subFieldType = mapSplit[1].replace(">", "").trim();
        return subFieldType;
    }

    private void extractPrimitiveTypes(TypeElement field, List<FieldValueMapping> completeFieldList, FieldElement subfield, boolean isArray) {
        if (isArray) {
            completeFieldList
                    .add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]", subfield.getType().replace(subfield.getType(),
                            protobufTypes.get(subfield.getType()))+ ARRAY_POSTFIX, 0, ""));
        } else {
            completeFieldList
                    .add(new FieldValueMapping(field.getName() + "." + subfield.getName(), subfield.getType().replace(subfield.getType(),
                            protobufTypes.get(subfield.getType())), 0, ""));
        }
    }

    private void extractDotTypesWhenIsInNestedType(TypeElement field, List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, boolean isArray, String dotType, List<String> imports) {
        List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(dotType), imports);
        for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
            String fieldValueMappingPrepared = getFieldValueMappingPrepared(fieldValueMapping);
            if ("enum".equals(fieldValueMapping.getFieldType())) {
                if (isArray) {
                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]", fieldValueMapping.getFieldType()+ARRAY_POSTFIX, 0, fieldValueMapping.getFieldValuesList().toString()));
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

    private void extractNestedTypes(TypeElement field, List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, boolean isArray, List<String> imports) {
        List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(subfield.getType()), imports);
        for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
            if ("enum".equals(fieldValueMapping.getFieldType())) {
                if (isArray) {
                    completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[]", fieldValueMapping.getFieldType()+ARRAY_POSTFIX, 0, fieldValueMapping.getFieldValuesList().toString()));
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

    private void extractNestedTypesMap(TypeElement field, List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, List<String> imports) {
        List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(extractInternalMapFields(subfield)), imports);
        for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
            String fieldValueMappingPrepared = getFieldValueMappingPrepared(fieldValueMapping);
            completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[:]." + fieldValueMappingPrepared, fieldValueMapping.getFieldType(), 0, ""));        }
    }


    private void extractDotTypesMap(TypeElement field, List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, String dotType, List<String> imports) {
        List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(dotType), imports);
        for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
            String fieldValueMappingPrepared = getFieldValueMappingPrepared(fieldValueMapping);
            completeFieldList.add(new FieldValueMapping(field.getName() + "." + subfield.getName() + "[:]." + fieldValueMappingPrepared, fieldValueMapping.getFieldType(), 0, ""));

        }
    }

    private void extractEnums(EnumElement field, List<FieldValueMapping> completeFieldList) {
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

    private String checkDotType(String subfieldType, List<String> imports) {
        String dotType = "";
        if (subfieldType.startsWith(".")) {
            String[] typeSplit = subfieldType.split("\\.");
            dotType = typeSplit[typeSplit.length - 1];
            dotType = !isExternalType(imports, dotType) ? dotType : subfieldType;
        }
        return dotType;
    }

    private boolean isExternalType(List<String> imports, String fieldType) {
        boolean externalType = false;
        for(String importType: imports) {
                Pattern pattern = Pattern.compile("(/([^/]+)\\.)");
                Matcher matcher = pattern.matcher(importType);
                if(matcher.find()) {
                    String extractedImportType = matcher.group(2);
                    if(extractedImportType != null) {
                        if(extractedImportType.toLowerCase().contains(fieldType.toLowerCase())
                                || fieldType.toLowerCase().contains(extractedImportType.toLowerCase())) {
                            externalType = true;
                        }
                    }
                }
            }
        return externalType;
    }

    private Field.Label checkNullLabel(FieldElement subfield) {
        Field.Label label;
        if (Objects.nonNull(subfield.getLabel())) {
            label = subfield.getLabel();
        } else {
            label = Field.Label.OPTIONAL;
        }
        return label;
    }

    private void fillNestedTypes(TypeElement field, HashMap<String, TypeElement> nestedTypes) {
        if (!field.getNestedTypes().isEmpty()) {
            field.getNestedTypes().forEach(nestedType ->
                    nestedTypes.put(nestedType.getName(), nestedType)
            );
        }
    }
}