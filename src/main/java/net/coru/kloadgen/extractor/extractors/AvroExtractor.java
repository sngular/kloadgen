package net.coru.kloadgen.extractor.extractors;

import com.kitfox.svg.A;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomObject;
import org.apache.avro.Schema;
import org.apache.commons.collections4.IteratorUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.apache.avro.Schema.Type.*;

public class AvroExtractor {

    public static final String ARRAY_POSTFIX = "-array";

    public static final String MAP_POSTFIX = "-map";

    private final Set<Schema.Type> typesSet = EnumSet.of(INT, DOUBLE, FLOAT, BOOLEAN, STRING, LONG, BYTES, FIXED);

    private final RandomObject randomObject = new RandomObject();

    public List<FieldValueMapping> processSchema(Schema schema) {
        List<FieldValueMapping> attributeList = new ArrayList<>();

        schema.getFields().forEach(field -> processField(field, attributeList));
        return attributeList;
    }

    public ParsedSchema getParsedSchema(String schema) {
        ParsedSchema parsed = new AvroSchema(schema);
        Schema schemaObj = (Schema) parsed.rawSchema();
        if(schemaObj.getType().equals(Schema.Type.UNION)) {
            Schema lastElement = schemaObj.getTypes().get(schemaObj.getTypes().size() -1);
            return new AvroSchema(lastElement.toString());
        }
        return parsed;
    }

    private List<FieldValueMapping> extractInternalFields(Schema.Field field, boolean isParentRequired) {
        return processFieldList(field.schema().getFields(), isParentRequired);
    }

    private List<FieldValueMapping> processFieldList(List<Schema.Field> fieldList, boolean isParentRequired) {
        List<FieldValueMapping> completeFieldList = new ArrayList<>();
        for (Schema.Field innerField : fieldList) {
            processField(innerField, completeFieldList, isParentRequired);
        }
        return completeFieldList;
    }

    private List<FieldValueMapping> extractArrayInternalFields(Schema.Field innerField, boolean isParentRequired) {
        return extractArrayInternalFields(innerField.name(), innerField.schema(), isParentRequired);
    }

    private List<FieldValueMapping> extractArrayInternalFields(String fieldName, Schema innerField, boolean isParentRequired) {
        List<FieldValueMapping> completeFieldList = new ArrayList<>();
        if (checkIfUnion(innerField)) {
            extractUnionRecord(fieldName, innerField, completeFieldList, isParentRequired);
        } else if (checkIfRecord(innerField)) {
            for (Schema.Field arrayElementField : innerField.getFields()) {
                processField(arrayElementField, completeFieldList, isParentRequired);
            }
            processRecordFieldList(fieldName, ".", completeFieldList);
        } else if (checkIfMap(innerField)) {
            completeFieldList.addAll(extractMapInternalFields(fieldName + "[:]", innerField.getValueType(), isParentRequired));
        } else if (checkIfArray(innerField)) {
            completeFieldList.addAll(extractArrayInternalFields(fieldName + "[]", innerField.getElementType(), isParentRequired));
        } else if (typesSet.contains(innerField.getType())) {
            completeFieldList.add(new FieldValueMapping(fieldName, innerField.getName() + ARRAY_POSTFIX, 0, "", isParentRequired, isParentRequired));
        }
        return completeFieldList;
    }

    private List<FieldValueMapping> extractMapInternalFields(String fieldName, Schema innerField, boolean isParentRequired) {
        List<FieldValueMapping> completeFieldList = new ArrayList<>();
        if (checkIfUnion(innerField)) {
            completeFieldList.add(new FieldValueMapping(fieldName, getNotNullType(innerField.getTypes()) + MAP_POSTFIX, 0, "", checkIfRequiredField(innerField), isParentRequired));
        } else if (checkIfRecord(innerField)) {
            if (innerField.getFields().size() > 1) {
                processRecordFieldList(fieldName, ".", processFieldList(innerField.getFields(), isParentRequired), completeFieldList);
            } else {
                processRecordFieldList(innerField.getName(), ".", extractInternalFields(innerField.getFields().get(0), isParentRequired), completeFieldList);
            }
        } else if (checkIfArray(innerField)) {
            List<FieldValueMapping> internalFields = extractArrayInternalFields(fieldName + "[]", innerField.getElementType(), isParentRequired);
            internalFields.forEach(field -> {
                if (field.getFieldType().endsWith(ARRAY_POSTFIX) && field.getFieldName().endsWith("[:][]")) {
                    tweakType(field, MAP_POSTFIX);
                }
            });
            completeFieldList.addAll(internalFields);
        } else if (checkIfMap(innerField) && !checkIfRecord(innerField.getValueType())) {
            List<FieldValueMapping> internalFields = extractMapInternalFields(fieldName + "[:]", innerField.getValueType(), isParentRequired);
            completeFieldList.addAll(internalFields);
        } else {
            completeFieldList.add(new FieldValueMapping(fieldName, innerField.getType().getName() + MAP_POSTFIX,0,"",isParentRequired, isParentRequired));
        }
        return completeFieldList;
    }

    public void processField(Schema.Field innerField, List<FieldValueMapping> completeFieldList, boolean isParentRequired) {
        if (checkIfRecord(innerField.schema())) {
            processRecordFieldList(innerField.name(), ".", extractInternalFields(innerField, isParentRequired), completeFieldList);
        } else if (checkIfArray(innerField.schema())) {
            List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField, isParentRequired);
            if (internalFields.size() == 1 && (internalFields.get(0).getFieldName().endsWith("[][:]") || internalFields.get(0).getFieldName().endsWith("[][]"))) {
                tweakType(internalFields.get(0), ARRAY_POSTFIX);
            }
            completeFieldList.addAll(internalFields);
        } else if (checkIfMap(innerField.schema())) {
            List<FieldValueMapping> internalFields = extractMapInternalFields(innerField.name() + "[:]", innerField.schema().getValueType(), isParentRequired);
            completeFieldList.addAll(internalFields);
        } else if (checkIfUnion(innerField.schema())) {
            extractUnionRecord(innerField.name(), innerField.schema(), completeFieldList, isParentRequired);
        } else {
            addFieldToList(innerField, completeFieldList, isParentRequired);
        }
    }

    public void processField(Schema.Field innerField, List<FieldValueMapping> completeFieldList) {
        if (checkIfRecord(innerField.schema())) {
            processRecordFieldList(innerField.name(), ".", extractInternalFields(innerField, true), completeFieldList);
        } else if (checkIfArray(innerField.schema())) {
            List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField, true);
            if (internalFields.size() == 1 && (internalFields.get(0).getFieldName().endsWith("[][:]") || internalFields.get(0).getFieldName().endsWith("[][]"))) {
                tweakType(internalFields.get(0), ARRAY_POSTFIX);
            }
            completeFieldList.addAll(internalFields);
        } else if (checkIfMap(innerField.schema())) {
            List<FieldValueMapping> internalFields = extractMapInternalFields(innerField.name() + "[:]", innerField.schema().getValueType(), true);
            completeFieldList.addAll(internalFields);
        } else if (checkIfUnion(innerField.schema())) {
            extractUnionRecord(innerField.name(), innerField.schema(), completeFieldList);
        } else {
            addFieldToList(innerField, completeFieldList);
        }
    }

    public void addFieldToList(Schema.Field innerField, List<FieldValueMapping> completeFieldList, boolean isParentRequired) {
        String typeName = innerField.schema().getType().getName();

        if (checkIfLogicalType(innerField.schema())) {
            typeName += "_" + innerField.schema().getLogicalType().getName();
        }

        if (checkIfEnumField(innerField.schema().getType())) {
            String fieldValueList = String.join(",", innerField.schema().getEnumSymbols());
            completeFieldList.add(new FieldValueMapping(innerField.name(), typeName, 0, fieldValueList, checkIfRequiredField(innerField.schema()), isParentRequired ));
        }else{
            completeFieldList.add(new FieldValueMapping(innerField.name(), typeName, 0, "", checkIfRequiredField(innerField.schema()), isParentRequired));
        }
    }


    public void addFieldToList(Schema.Field innerField, List<FieldValueMapping> completeFieldList) {
        String typeName = innerField.schema().getType().getName();

        if (checkIfLogicalType(innerField.schema())) {
            typeName += "_" + innerField.schema().getLogicalType().getName();
        }

        if (checkIfEnumField(innerField.schema().getType())) {
            String fieldValueList = String.join(",", innerField.schema().getEnumSymbols());
            completeFieldList.add(new FieldValueMapping(innerField.name(), typeName, 0, fieldValueList ));
        }else{
        completeFieldList.add(new FieldValueMapping(innerField.name(), typeName));
        }
    }

    private boolean checkIfLogicalType(Schema innerSchema) {
        return Objects.nonNull(innerSchema.getLogicalType());
    }

    private boolean checkIfRequiredField(Schema innerSchema){
        if (checkIfUnion(innerSchema)){
            return !IteratorUtils.matchesAny(innerSchema.getTypes().iterator(), type -> type.getType() == NULL);
        } else {
            return true;
        }
    }

    private void extractUnionRecord(String fieldName, Schema innerField, List<FieldValueMapping> completeFieldList, boolean isParentRequired) {
        Schema recordUnion = getRecordUnion(innerField.getTypes());
        if (null != recordUnion) {
            if (typesSet.contains(recordUnion.getType())){
                completeFieldList.add(new FieldValueMapping(fieldName, getNotNullType(innerField.getTypes()),0,"",false, isParentRequired));
            } else if(recordUnion.getType().equals(ARRAY) && typesSet.contains(recordUnion.getElementType().getType())){
                completeFieldList.add(new FieldValueMapping(fieldName + "[]", getNotNullType(innerField.getTypes()),0,"",false, isParentRequired));
            } else if(recordUnion.getType().equals(MAP) && typesSet.contains(recordUnion.getValueType().getType())){
                completeFieldList.add(new FieldValueMapping(fieldName + "[:]", getNotNullType(innerField.getTypes()),0,"",false, isParentRequired));
            }  else{
                processField(fieldName, completeFieldList, recordUnion, isParentRequired);
            }
        } else {
            completeFieldList.add(new FieldValueMapping(innerField.getName(), getNotNullType(innerField.getTypes()),0,"",checkIfRequiredField(innerField),isParentRequired));
        }
    }

    private void extractUnionRecord(String fieldName, Schema innerField, List<FieldValueMapping> completeFieldList) {
        Schema recordUnion = getRecordUnion(innerField.getTypes());
        if (null != recordUnion) {
            if (typesSet.contains(recordUnion.getType())){
                completeFieldList.add(new FieldValueMapping(fieldName, getNotNullType(innerField.getTypes()),0,"",false, false));
            } else if(recordUnion.getType().equals(ARRAY) && typesSet.contains(recordUnion.getElementType().getType())){
                completeFieldList.add(new FieldValueMapping(fieldName + "[]", getNotNullType(innerField.getTypes()),0,"",false, false));
            } else if(recordUnion.getType().equals(MAP) && typesSet.contains(recordUnion.getValueType().getType())){
                completeFieldList.add(new FieldValueMapping(fieldName + "[:]", getNotNullType(innerField.getTypes()),0,"",false, false));
            } else{
                processField(fieldName, completeFieldList, recordUnion, false);
            }

        } else {
            completeFieldList.add(new FieldValueMapping(innerField.getName(), getNotNullType(innerField.getTypes()),0,"",checkIfRequiredField(innerField),true));
        }
    }

    private void processField(String fieldName, List<FieldValueMapping> completeFieldList, Schema recordUnion, boolean isParentRequired) {
        if (checkIfRecord(recordUnion)) {
            processRecordFieldList(fieldName, ".", processFieldList(recordUnion.getFields(), isParentRequired), completeFieldList);
        } else if (checkIfArray(recordUnion)) {
            extractArray(fieldName, completeFieldList, recordUnion.getElementType(), isParentRequired);
        } else if (checkIfMap(recordUnion)) {
            List<FieldValueMapping> internalFields = extractMapInternalFields(fieldName + "[:]", recordUnion.getValueType(), isParentRequired);
            if (internalFields.size() == 1 && internalFields.get(0).getFieldName().endsWith("[:][:]")) {
                tweakType(internalFields.get(0), MAP_POSTFIX);
            }
            completeFieldList.addAll(internalFields);
        } else {
            completeFieldList.add(new FieldValueMapping(fieldName, recordUnion.getType().getName(),0,"", checkIfRequiredField(recordUnion), isParentRequired));
        }
    }

    private void extractArray(String fieldName, List<FieldValueMapping> completeFieldList, Schema recordUnion, boolean isParentRequired) {
        List<FieldValueMapping> internalFields = extractArrayInternalFields(fieldName, recordUnion, isParentRequired);
        if (checkIfRecord(recordUnion)) {
            processRecordFieldList(fieldName, "[].", internalFields, completeFieldList);
        } else if (checkIfMap(recordUnion)) {
            if (Objects.requireNonNull(recordUnion.getType()).equals(MAP)) {
                internalFields.forEach(field -> {
                    field.setFieldName(field.getFieldName().replace("[:]", "[][:]"));
                    if (field.getFieldName().matches(".*\\[:?](\\[:?])?$")) {
                        tweakType(field, ARRAY_POSTFIX);
                    }
                    completeFieldList.add(field);
                });
            } else {
                createArrayType(completeFieldList, internalFields, fieldName + "[]");
            }
        } else if (checkIfArray(recordUnion)) {
            tweakType(internalFields.get(0), ARRAY_POSTFIX);
            createArrayType(completeFieldList, internalFields, fieldName + "[]");
        } else {
            renameArrayType(completeFieldList, internalFields, fieldName + "[]");
        }
    }

    private void tweakType(FieldValueMapping internalField, String postfix) {
        internalField.setFieldType(internalField.getFieldType() + postfix);
    }

    private void createArrayType(List<FieldValueMapping> completeFieldList, List<FieldValueMapping> internalFields, String fieldName) {

        internalFields.forEach(internalField -> {
            if (!internalField.getFieldName().contains("[][:]") && !internalField.getFieldName().contains("[][]")) {
                internalField.setFieldName(internalField.getFieldName().replace(fieldName, fieldName + "[:]"));
            }
        });
        completeFieldList.addAll(internalFields);
    }

    private void renameArrayType(List<FieldValueMapping> completeFieldList, List<FieldValueMapping> internalFields, String fieldName) {
        internalFields.forEach(internalField -> internalField.setFieldName(fieldName));
        completeFieldList.addAll(internalFields);
    }

    private boolean checkIfRecord(Schema innerSchema) {
        return RECORD.equals(innerSchema.getType());
    }

    private boolean checkIfMap(Schema innerSchema) {
        return MAP.equals(innerSchema.getType());
    }

    private boolean checkIfArray(Schema innerSchema) {
        return ARRAY.equals(innerSchema.getType());
    }

    private boolean checkIfUnion(Schema innerSchema) {
        return UNION.equals(innerSchema.getType());
    }

    private String getNotNullType(List<Schema> types) {
        Schema chosenType = extractTypeName(types.get(0)).equalsIgnoreCase("null") ? types.get(1) : types.get(0);
        chosenType = extractTypeName(types.get(1)).equalsIgnoreCase("array") ? types.get(1) : chosenType;
        String chosenTypeName = extractTypeName(chosenType);

        if (!randomObject.isTypeValid(chosenTypeName)) {
            chosenTypeName = "null";
        } else if ("array".equalsIgnoreCase(chosenTypeName)) {
            chosenTypeName = chosenType.getElementType().getName() + ARRAY_POSTFIX;
        } else if ("map".equalsIgnoreCase(chosenTypeName)) {
            chosenTypeName = chosenType.getValueType().getName() + MAP_POSTFIX;
        }
        return chosenTypeName;
    }

    private Schema getRecordUnion(List<Schema> types) {
        Schema isRecord = null;
        for (Schema schema : types) {
            if (RECORD == schema.getType() || ARRAY == schema.getType() || MAP == schema.getType() || typesSet.contains(schema.getType())) {
                isRecord = schema;
            }
        }
        return isRecord;
    }

    private boolean isUnionLastChildren(List<Schema> types){
        boolean isLast = false;
        for (Schema schema : types) {
            if (typesSet.contains(schema.getType())  && (RECORD != schema.getType() || ARRAY != schema.getType() || MAP != schema.getType())) {
                isLast = true;
            }else{
                isLast = false;
            }
        }
        return isLast;
    }

    private void processRecordFieldList(String fieldName, String splitter, List<FieldValueMapping> internalFields, List<FieldValueMapping> completeFieldList) {
        internalFields.forEach(internalField -> {
            if (internalField.getFieldName().startsWith(fieldName + ".")) {
                internalField.setFieldName(fieldName + internalField.getFieldName().replace(fieldName, splitter.replace(".", "")));
            } else {
                internalField.setFieldName(fieldName + splitter + internalField.getFieldName());
            }
            completeFieldList.add(internalField);
        });
    }

    private void processRecordFieldList(String fieldName, String splitter, List<FieldValueMapping> internalFields) {
        internalFields.forEach(internalField -> internalField.setFieldName(fieldName + splitter + internalField.getFieldName()));
    }

    private String extractTypeName(Schema schema) {
        return schema.getType().getName();
    }

    private boolean checkIfEnumField(Schema.Type type){
        return type.equals(ENUM);
    }
}
