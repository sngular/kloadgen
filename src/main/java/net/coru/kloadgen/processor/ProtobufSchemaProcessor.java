package net.coru.kloadgen.processor;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections4.IteratorUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.apache.avro.Schema.Type.*;


public class ProtobufSchemaProcessor extends SchemaProcessorLib {

    private ProtoFileElement schema;
    private SchemaMetadata metadata;
    private RandomObject randomObject;
    private RandomMap randomMap;
    private List<FieldValueMapping> fieldExprMappings;


    public void processSchema(ProtoFileElement schema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
        this.schema = schema;
        this.fieldExprMappings = fieldExprMappings;
        this.metadata = metadata;
        randomObject = new RandomObject();

    }

    public void processSchema(ParsedSchema parsedSchema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
        this.schema = (ProtoFileElement) parsedSchema.rawSchema();
        this.fieldExprMappings = fieldExprMappings;
        this.metadata = metadata;
        randomObject = new RandomObject();
        randomMap = new RandomMap();
    }

    public EnrichedRecord next() {
        ProtobufSchema protobufSchema = getProtobufSchema();
        DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(protobufSchema.toDescriptor());
        DynamicMessage message;
        if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
            HashMap<String, Descriptors.Descriptor> nestedTypes = new HashMap<>();
            fillNestedTypes(messageBuilder.getDescriptorForType().getNestedTypes(), nestedTypes);
            ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
            FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

            while (!fieldExpMappingsQueue.isEmpty()) {
                String cleanPath = cleanUpPath(fieldValueMapping, "");
                String fieldName = createFieldName(cleanPath);
                String typeName = createTypeName(cleanPath);
                if (cleanPath.contains("][")) {
                    if (checkIfArrayMap(Objects.requireNonNull(fieldValueMapping).getFieldType())) {
                        fieldValueMapping = processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, messageBuilder, fieldName);
                    } else if (checkIfMapArray(fieldValueMapping.getFieldType())) {
                        fieldValueMapping = processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, messageBuilder, fieldName);
                    } else if (checkIfIsRecordMapArray(cleanPath)) {
                        fieldValueMapping = processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, messageBuilder, fieldName);
                    } else if (checkIfIsRecordArrayMap(cleanPath)) {
                        fieldValueMapping = processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, messageBuilder, fieldName);
                    } else {
                        throw new KLoadGenException("Wrong configuration Map - Array");
                    }
                } else if (cleanPath.contains("[")) {
                    if (checkIfMap(Objects.requireNonNull(fieldValueMapping).getFieldType())) {
                        fieldValueMapping = processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, messageBuilder, fieldName);
                    } else if (checkIfArray(fieldValueMapping.getFieldType())) {
                        fieldValueMapping = processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, messageBuilder, typeName);
                    } else if (checkIfRecordArray(cleanPath)) {
                        fieldValueMapping = processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, messageBuilder, fieldName);
                    } else if (checkIfRecordMap(cleanPath)) {
                        fieldValueMapping = processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, messageBuilder, fieldName);
                    } else {
                        throw new KLoadGenException("Wrong configuration Map - Array");
                    }
                } else if (cleanPath.contains(".")) {
                    fieldValueMapping = proccessFieldValueMapping(messageBuilder, fieldExpMappingsQueue, fieldValueMapping, cleanPath, typeName, fieldName);
                } else {
                    throw new KLoadGenException("Something Odd just happened");
                }
            }
        }

        message = messageBuilder.build();
        return new EnrichedRecord(metadata, message.getAllFields());
    }

    private FieldValueMapping proccessFieldValueMapping(DynamicMessage.Builder messageBuilder, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, FieldValueMapping fieldValueMapping, String cleanPath, String typeName, String fieldName) {
        if (checkComplexObject(cleanPath)) {
            messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(typeName), createObject(getDescriptorForField(messageBuilder, typeName), typeName, fieldExpMappingsQueue));
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
        } else {
            messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(fieldName),
                    randomObject.generateRandom(
                            fieldValueMapping.getFieldType(),
                            fieldValueMapping.getValueLength(),
                            fieldValueMapping.getFieldValuesList(),
                            fieldValueMapping.getConstrains()
                    )
            );
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
            fieldExpMappingsQueue.remove();
            fieldValueMapping = fieldExpMappingsQueue.peek();
        }
        return fieldValueMapping;
    }

    private String createTypeName(String cleanPath) {
        String[] cleanPathSplitted = cleanPath.split("\\.");
        String typeName;
        if (cleanPathSplitted.length > 2) {
            typeName = cleanPathSplitted[cleanPathSplitted.length - (cleanPathSplitted.length - 1)];
        } else {
            typeName = cleanPathSplitted[0];
        }
        return typeName;
    }

    private String createFieldName(String cleanPath) {
        String[] cleanPathSplitted = cleanPath.split("\\.");
        String fieldName;
        if (cleanPathSplitted.length > 2) {
            fieldName = cleanPathSplitted[cleanPathSplitted.length - (cleanPathSplitted.length - 2)];
        } else {
            fieldName = cleanPathSplitted[1];
        }
        return fieldName;
    }

    public boolean checkComplexObject(String cleanPath) {
        String[] cleanPathSplitted = cleanPath.split("\\.");
        return cleanPathSplitted.length > 2;
    }

    private DynamicMessage createObject(final Descriptors.Descriptor subMessageDescriptor, final String fieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

        DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(subMessageDescriptor);
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        while (!fieldExpMappingsQueue.isEmpty()
                && (Objects.requireNonNull(fieldValueMapping).getFieldName().matches(".*" + fieldName + "$")
                || fieldValueMapping.getFieldName().matches(fieldName + "\\..*")
                || fieldValueMapping.getFieldName().matches(".*" + fieldName + "\\[.*")
                || fieldValueMapping.getFieldName().matches(".*" + fieldName + "\\..*"))) {
            String cleanFieldName = cleanUpPath(fieldValueMapping, fieldName);
            if (cleanFieldName.contains("][") && !fieldValueMapping.getFieldType().endsWith("map-map") && !fieldValueMapping.getFieldType().endsWith("array-array")) {
                if (checkIfMapArray(fieldValueMapping.getFieldType())) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if (checkIfArrayMap(fieldValueMapping.getFieldType())) {
                    String fieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if (checkIfIsRecordMapArray(cleanFieldName)) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if (checkIfIsRecordArrayMap(cleanFieldName)) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                }
            } else if (cleanFieldName.endsWith("]")) {
                if (checkIfMap(fieldValueMapping.getFieldType())) {
                    String fieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if (checkIfArray(fieldValueMapping.getFieldType())) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if (checkIfRecordMap(cleanFieldName)) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if (checkIfRecordArray(cleanFieldName)) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                }
            } else if (cleanFieldName.contains(".")) {
                String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldNameSubEntity),
                        createObject(getDescriptorForField(messageBuilder, fieldNameSubEntity),
                                fieldNameSubEntity,
                                fieldExpMappingsQueue));
            } else {
                fieldExpMappingsQueue.poll();
                messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(cleanFieldName),
                        randomObject.generateRandom(
                                fieldValueMapping.getFieldType(),
                                fieldValueMapping.getValueLength(),
                                fieldValueMapping.getFieldValuesList(),
                                fieldValueMapping.getConstrains()
                        )
                );
            }
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
        }
        return messageBuilder.build();
    }

    @NotNull
    private ProtobufSchema getProtobufSchema() {
        String schemaToString = schema.toSchema();
        return new ProtobufSchema(schemaToString);
    }

    private FieldValueMapping processFieldValueMappingAsRecordArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), getCleanMethodName(fieldValueMapping, fieldName));
        createObjectArray(messageBuilder,
                fieldName,
                arraySize,
                fieldExpMappingsQueue);
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsRecordMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), getCleanMethodName(fieldValueMapping, fieldName));

        messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(fieldName), createObjectMap(messageBuilder,
                fieldName,
                mapSize,
                fieldExpMappingsQueue));
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsSimpleArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String typeName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        String cleanPath = cleanUpPath(fieldValueMapping, "");
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), typeName);
        String createdTypeName = createTypeName(cleanPath);
        String fieldName = createFieldName(cleanPath);
        if(createdTypeName.contains("[")){
            createdTypeName = createdTypeName.substring(0, createdTypeName.indexOf("["));
        }
        String cleanFieldName = fieldName.substring(0, fieldName.indexOf("["));
        if (Objects.nonNull(messageBuilder.getDescriptorForType().findFieldByName(createdTypeName)) && messageBuilder.getDescriptorForType().findFieldByName(createdTypeName).getType().equals(Descriptors.FieldDescriptor.Type.MESSAGE)) {
            messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(createdTypeName), createObject(messageBuilder.getDescriptorForType().findFieldByName(createdTypeName).getMessageType(), createdTypeName, fieldExpMappingsQueue));
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
        } else {
            messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(cleanFieldName),
                    createArray(cleanFieldName, arraySize, fieldExpMappingsQueue));
            }
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsSimpleMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        fieldExpMappingsQueue.remove();
        messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName),
                createSimpleTypeMap(fieldName, fieldValueMapping.getFieldType(),
                        calculateMapSize(fieldValueMapping.getFieldName(), fieldName),
                        fieldValueMapping.getValueLength(),
                        fieldValueMapping.getFieldValuesList()));
        return fieldExpMappingsQueue.peek();
    }

    private FieldValueMapping processFieldValueMappingAsSimpleArrayMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        fieldExpMappingsQueue.remove();
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
        Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
        var simpleTypeArrayMap = createSimpleTypeArrayMap(fieldName, fieldValueMapping.getFieldType(), arraySize, mapSize, fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList());
        messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), simpleTypeArrayMap);
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsSimpleMapArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
        Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

        var mapArray = randomMap.generateMap(fieldValueMapping.getFieldType(), mapSize, fieldValueMapping.getFieldValuesList(), fieldValueMapping.getValueLength(), arraySize, fieldValueMapping.getConstrains());

        messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), mapArray);
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsRecordArrayMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
        Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

        Map<String, List> recordMapArray = new HashMap<>(mapSize);
        for (int i = 0; i < mapSize - 1; i++) {
            ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
            recordMapArray.put((String) randomObject.generateRandom("string", fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                    createComplexObjectArray(messageBuilder, fieldName, arraySize, temporalQueue));
        }
        recordMapArray.put((String) randomObject.generateRandom("string", fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                createComplexObjectArray(messageBuilder, fieldName, arraySize, fieldExpMappingsQueue));
        messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), recordMapArray);
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsRecordMapArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
        Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
        var recordArrayMap = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize - 1; i++) {

            recordArrayMap.add(createObjectMap(messageBuilder, fieldName, mapSize, fieldExpMappingsQueue.clone()));
        }
        recordArrayMap.add(createObjectMap(messageBuilder, fieldName, arraySize, fieldExpMappingsQueue));
        messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), recordArrayMap);
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private Schema extractType(Schema.Field field, Schema.Type typeToMatch) {
        Schema realField = field.schema();
        if (UNION.equals(field.schema().getType())) {
            realField = IteratorUtils.find(field.schema().getTypes().iterator(), type -> typeToMatch.equals(type.getType()));
        }
        return realField;
    }

    private void processSimpleTypes(DynamicMessage.Builder messageBuilder, FieldValueMapping fieldValueMapping, String fieldName) {
        if (fieldName.endsWith("[]")) {
            processArray(messageBuilder, fieldValueMapping, fieldName);
        } else {
            messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName),
                    randomObject.generateRandom(
                            fieldValueMapping.getFieldType(),
                            fieldValueMapping.getValueLength(),
                            fieldValueMapping.getFieldValuesList(),
                            fieldValueMapping.getConstrains()));
        }
    }

    private void processArray(DynamicMessage.Builder messageBuilder, FieldValueMapping fieldValueMapping, String fieldType) {
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldType);
        String arrayCleanPath = fieldType.substring(0, fieldType.indexOf("["));
        for (int i = 0; i < arraySize; i++) {
            messageBuilder.addRepeatedField(getFieldDescriptorForField(messageBuilder, arrayCleanPath),
                    randomObject.generateRandom(
                            fieldValueMapping.getFieldType(),
                            fieldValueMapping.getValueLength(),
                            fieldValueMapping.getFieldValuesList(),
                            fieldValueMapping.getConstrains()));
        }
    }

    private void fillNestedTypes(List<Descriptors.Descriptor> descriptorNestedType, HashMap<String, Descriptors.Descriptor> nestedTypes) {

        for (Descriptors.Descriptor descriptor : descriptorNestedType) {
            nestedTypes.put(descriptor.getName(), descriptor);
        }

    }


    private void createObjectArray(DynamicMessage.Builder messageBuilder, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

        for (int i = 0; i < arraySize - 1; i++) {
            messageBuilder.addRepeatedField(getFieldDescriptorForField(messageBuilder, fieldName),
                    createObject(getDescriptorForField(messageBuilder, fieldName), fieldName, fieldExpMappingsQueue.clone()));
        }
        messageBuilder.addRepeatedField(getFieldDescriptorForField(messageBuilder, fieldName),
                createObject(getDescriptorForField(messageBuilder, fieldName), fieldName, fieldExpMappingsQueue));
    }

    private List<DynamicMessage> createComplexObjectArray(DynamicMessage.Builder messageBuilder, String fieldName, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        List<DynamicMessage> objectArray = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize - 1; i++) {
            ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
            objectArray.add(createObject(messageBuilder.getDescriptorForType(), fieldName, temporalQueue));
        }
        objectArray.add(createObject(messageBuilder.getDescriptorForType(), fieldName, fieldExpMappingsQueue));
        return objectArray;
    }

    private List<Message> createObjectMap(DynamicMessage.Builder messageBuilder, String fieldName, Integer mapSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        List<Message> messageMap = new ArrayList<>();
        Descriptors.FieldDescriptor descriptor = getFieldDescriptorForField(messageBuilder, fieldName);

        for (int i = 0; i < mapSize - 1; i++) {
            messageMap.add(buildMapEntry(descriptor, fieldName, fieldExpMappingsQueue.clone()));
        }

        messageMap.add(buildMapEntry(descriptor, fieldName, fieldExpMappingsQueue));
        messageBuilder.setField(descriptor, messageMap);
        return messageMap;
    }

    private Message buildMapEntry(Descriptors.FieldDescriptor descriptor, String fieldName, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor.getMessageType());
        Descriptors.FieldDescriptor keyFieldDescriptor = descriptor.getMessageType().findFieldByName("key");
        builder.setField(keyFieldDescriptor,
                randomObject.generateRandom("string", 10, Collections.emptyList(), Collections.emptyMap()));
        Descriptors.FieldDescriptor valueFieldDescriptor = descriptor.getMessageType().findFieldByName("value");
        builder.setField(valueFieldDescriptor,
                createObject(valueFieldDescriptor.getMessageType(), fieldName, fieldExpMappingsQueue.clone()));
        return builder.build();
    }

    private Descriptors.Descriptor getDescriptorForField(DynamicMessage.Builder messageBuilder, String typeName) {
        return messageBuilder.getDescriptorForType().findFieldByName(typeName).getMessageType();
    }

    private Descriptors.FieldDescriptor getFieldDescriptorForField(DynamicMessage.Builder messageBuilder, String typeName) {
        return messageBuilder.getDescriptorForType().findFieldByName(typeName);
    }
}
