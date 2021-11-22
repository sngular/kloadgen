package net.coru.kloadgen.extractor.extractors;

import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomObject;
import org.apache.avro.Schema;

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

    private List<FieldValueMapping> extractInternalFields(Schema.Field field) {
        return processFieldList(field.schema().getFields());
    }

    private List<FieldValueMapping> processFieldList(List<Schema.Field> fieldList) {
        List<FieldValueMapping> completeFieldList = new ArrayList<>();
        for (Schema.Field innerField : fieldList) {
            processField(innerField, completeFieldList);
        }
        return completeFieldList;
    }

    private List<FieldValueMapping> extractArrayInternalFields(Schema.Field innerField) {
        return extractArrayInternalFields(innerField.name(), innerField.schema());
    }

    private List<FieldValueMapping> extractArrayInternalFields(String fieldName, Schema innerField) {
        List<FieldValueMapping> completeFieldList = new ArrayList<>();
        if (checkIfRecord(innerField)) {
            for (Schema.Field arrayElementField : innerField.getFields()) {
                processField(arrayElementField, completeFieldList);
            }
            processRecordFieldList(fieldName, ".", completeFieldList);
        } else if (checkIfMap(innerField)) {
            completeFieldList.addAll(extractMapInternalFields(fieldName + "[:]", innerField.getValueType()));
        } else if (checkIfArray(innerField)) {
            completeFieldList.addAll(extractArrayInternalFields(fieldName + "[]", innerField.getElementType()));
        } else if (typesSet.contains(innerField.getType())) {
            completeFieldList.add(new FieldValueMapping(fieldName, innerField.getName() + ARRAY_POSTFIX));
        }
        return completeFieldList;
    }

    private List<FieldValueMapping> extractMapInternalFields(String fieldName, Schema innerField) {
        List<FieldValueMapping> completeFieldList = new ArrayList<>();
        if (checkIfUnion(innerField)) {
            completeFieldList.add(new FieldValueMapping(fieldName, getNotNullType(innerField.getTypes()) + MAP_POSTFIX));
        } else if (checkIfRecord(innerField)) {
            if (innerField.getFields().size() > 1) {
                processRecordFieldList(fieldName, ".", processFieldList(innerField.getFields()), completeFieldList);
            } else {
                processRecordFieldList(innerField.getName(), ".", extractInternalFields(innerField.getFields().get(0)), completeFieldList);
            }
        } else if (checkIfArray(innerField)) {
            List<FieldValueMapping> internalFields = extractArrayInternalFields(fieldName + "[]", innerField.getElementType());
            internalFields.forEach(field -> {
                if (field.getFieldType().endsWith(ARRAY_POSTFIX) && field.getFieldName().endsWith("[:][]")) {
                    tweakType(field, MAP_POSTFIX);
                }
            });
            completeFieldList.addAll(internalFields);
            //extractedArrayCollection(innerField, completeFieldList, internalFields);
        } else if (checkIfMap(innerField) && !checkIfRecord(innerField.getValueType())) {
            List<FieldValueMapping> internalFields = extractMapInternalFields(fieldName + "[:]", innerField.getValueType());
            completeFieldList.addAll(internalFields);
            // extractedMapCollection(innerField, completeFieldList, internalFields);
        } else {
            completeFieldList.add(new FieldValueMapping(fieldName, innerField.getType().getName() + MAP_POSTFIX));
        }
        return completeFieldList;
    }

    public void processField(Schema.Field innerField, List<FieldValueMapping> completeFieldList) {
        if (checkIfRecord(innerField.schema())) {
            processRecordFieldList(innerField.name(), ".", extractInternalFields(innerField), completeFieldList);
        } else if (checkIfArray(innerField.schema())) {
            List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField);
            if (internalFields.size() == 1 && (internalFields.get(0).getFieldName().endsWith("[][:]") || internalFields.get(0).getFieldName().endsWith("[][]"))) {
                tweakType(internalFields.get(0), ARRAY_POSTFIX);
            }
            completeFieldList.addAll(internalFields);
        } else if (checkIfMap(innerField.schema())) {
            List<FieldValueMapping> internalFields = extractMapInternalFields(innerField.name() + "[:]", innerField.schema().getValueType());
            completeFieldList.addAll(internalFields);
        } else if (checkIfUnion(innerField.schema())) {
            extractUnionRecord(innerField, completeFieldList);
        } else {
            addFieldToList(innerField, completeFieldList);
        }
    }


    public void addFieldToList(Schema.Field innerField, List<FieldValueMapping> completeFieldList) {
        String typeName = innerField.schema().getType().getName();

        if (checkIfLogicalType(innerField.schema())) {
            typeName += "_" + innerField.schema().getLogicalType().getName();
        }

        if (checkIfEnumField(innerField.schema().getType())) {
            String fieldValueList = String.join(",", innerField.schema().getEnumSymbols());
            completeFieldList.add(new FieldValueMapping(innerField.name(), typeName, 0, fieldValueList));
        }else{
        completeFieldList.add(new FieldValueMapping(innerField.name(), typeName));
        }
    }

    private boolean checkIfLogicalType(Schema innerSchema) {
        return Objects.nonNull(innerSchema.getLogicalType());
    }

    private void extractUnionRecord(Schema.Field innerField, List<FieldValueMapping> completeFieldList) {
        Schema recordUnion = getRecordUnion(innerField.schema().getTypes());
        if (null != recordUnion) {
            processField(innerField, completeFieldList, recordUnion);
        } else {
            completeFieldList.add(new FieldValueMapping(innerField.name(), getNotNullType(innerField.schema().getTypes())));
        }
    }

    private void processField(Schema.Field innerField, List<FieldValueMapping> completeFieldList, Schema recordUnion) {
        if (checkIfRecord(recordUnion)) {
            processRecordFieldList(innerField.name(), ".", processFieldList(recordUnion.getFields()), completeFieldList);
        } else if (checkIfArray(recordUnion)) {
            extractArray(innerField, completeFieldList, recordUnion.getElementType());
        } else if (checkIfMap(recordUnion)) {
            List<FieldValueMapping> internalFields = extractMapInternalFields(innerField.name() + "[:]", recordUnion.getValueType());
            if (internalFields.size() == 1 && internalFields.get(0).getFieldName().endsWith("[:][:]")) {
                tweakType(internalFields.get(0), MAP_POSTFIX);
            }
            completeFieldList.addAll(internalFields);
        } else {
            completeFieldList.add(new FieldValueMapping(innerField.name(), recordUnion.getType().getName()));
        }
    }

    private void extractArray(Schema.Field innerField, List<FieldValueMapping> completeFieldList, Schema recordUnion) {
        List<FieldValueMapping> internalFields = extractArrayInternalFields(innerField.name(), recordUnion);
        if (checkIfRecord(recordUnion)) {
            processRecordFieldList(innerField.name(), "[].", internalFields, completeFieldList);
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
                createArrayType(completeFieldList, internalFields, innerField.name() + "[]");
            }
        } else if (checkIfArray(recordUnion)) {
            tweakType(internalFields.get(0), ARRAY_POSTFIX);
            createArrayType(completeFieldList, internalFields, innerField.name() + "[]");
        } else {
            renameArrayType(completeFieldList, internalFields, innerField.name() + "[]");
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
            chosenTypeName = "int-array";
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
