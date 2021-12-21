package net.coru.kloadgen.processor;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.TypeElement;
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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.MAP;


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
                String fieldName = getCleanMethodName(fieldValueMapping, "");
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
                        fieldValueMapping = processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue,messageBuilder, fieldName);
                    } else if (checkIfRecordArray(cleanPath)) {
                        fieldValueMapping = processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, messageBuilder, fieldName);
                    } else if (checkIfRecordMap(cleanPath)) {
                        fieldValueMapping = processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, messageBuilder, fieldName);
                    } else {
                        throw new KLoadGenException("Wrong configuration Map - Array");
                    }
                } else if (cleanPath.contains(".")) {
                    messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), createObject(getDescriptorForField(messageBuilder, fieldName), fieldName, fieldExpMappingsQueue));
                    fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
                } else {
                  /*  messageBuilder.setField(Objects.requireNonNull(fieldValueMapping).getFieldName(),
                        protoGeneratorTool.generateObject(
                            entity.getSchema().getField(fieldName),
                            fieldValueMapping.getFieldType(),
                            fieldValueMapping.getValueLength(),
                            fieldValueMapping.getFieldValuesList(),
                            extractConstrains(schema.getField(fieldValueMapping.getFieldName()))
                        )
                    );*/
                    fieldExpMappingsQueue.remove();
                    fieldValueMapping = fieldExpMappingsQueue.peek();
                }
               /* if (messageBuilder.getDescriptorForType().findFieldByName(typeName) != null){
                    Descriptors.Descriptor subDescriptor = messageBuilder.getDescriptorForType().findFieldByName(typeName).getMessageType();
                    if (nestedTypes.containsKey(subDescriptor.getName())) {
                        DynamicMessage.Builder subMessageBuilder = DynamicMessage.newBuilder(subDescriptor);
                        messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(typeName),  createObject(subMessageBuilder, fieldValueMapping, fieldName));
                    }
                } else {
                    processSimpleTypes(messageBuilder, fieldValueMapping, fieldName);
                }*/
                fieldExpMappingsQueue.remove();
                fieldValueMapping = fieldExpMappingsQueue.peek();
            }
        }

        message = messageBuilder.build();
        return new EnrichedRecord(metadata, message.getAllFields());
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
            if (cleanFieldName.contains("][") && !fieldValueMapping.getFieldType().endsWith("map-map") && !fieldValueMapping.getFieldType().endsWith("array-array") ) {
                if (checkIfMapArray(fieldValueMapping.getFieldType())) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if (checkIfArrayMap(fieldValueMapping.getFieldType())) {
                    String fieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                }else if(checkIfIsRecordMapArray(cleanFieldName)){
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue , messageBuilder, fieldNameSubEntity );
                }else if(checkIfIsRecordArrayMap(cleanFieldName)){
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue , messageBuilder, fieldNameSubEntity );
                }
            } else if(cleanFieldName.endsWith("]")){
                if (checkIfMap(fieldValueMapping.getFieldType())) {
                    String fieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if(checkIfArray(fieldValueMapping.getFieldType())){
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if(checkIfRecordMap(cleanFieldName)){
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                }else if(checkIfRecordArray(cleanFieldName)){
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                }
            }
            else if (cleanFieldName.contains(".")) {
                String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldNameSubEntity),
                    createObject(getDescriptorForField(messageBuilder, fieldNameSubEntity),
                    fieldNameSubEntity,
                    fieldExpMappingsQueue));
            } else {
                fieldExpMappingsQueue.poll();
                messageBuilder.setField(getFieldDescriptorForField(messageBuilder, cleanFieldName),
                    protoGeneratorTool.generateObject(
                        getFieldDescriptorForField(messageBuilder, cleanFieldName),
                        fieldValueMapping.getFieldType(),
                        fieldValueMapping.getValueLength(),
                        fieldValueMapping.getFieldValuesList(),
                        extractConstrains(messageBuilder.getSchema().getField(cleanFieldName))
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

        messageBuilder.setField(fieldName, createObjectMap(extractType(entity.getSchema().getField(fieldName), MAP).getValueType(),
            fieldName,
            mapSize,
            fieldExpMappingsQueue));
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsSimpleArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
        messageBuilder.setField(messageBuilder.,
            createArray(fieldName, arraySize, fieldExpMappingsQueue));
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsSimpleMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        fieldExpMappingsQueue.remove();
        // Add condition that checks (][)
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

        var mapArray = randomMap.generateMap(fieldValueMapping.getFieldType(), mapSize, fieldValueMapping.getFieldValuesList(),fieldValueMapping.getValueLength(), arraySize, fieldValueMapping.getConstrains());

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
                createObjectArray(messageBuilder, fieldName, arraySize, temporalQueue));
        }
        recordMapArray.put((String) randomObject.generateRandom("string", fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
            createObjectArray(messageBuilder, fieldName, arraySize, fieldExpMappingsQueue));
        messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), recordMapArray);
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsRecordMapArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
        Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
        var recordArrayMap = new ArrayList<>(arraySize);
        // Extract inner messagebuilder
        for (int i = 0; i < arraySize - 1; i++) {

            recordArrayMap.add(createObjectMap(messageBuilder, fieldName, mapSize, fieldExpMappingsQueue.clone()));
        }
        recordArrayMap.add(createObjectMap(messageBuilder, fieldName, arraySize, fieldExpMappingsQueue));
        messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), recordArrayMap);
        return getSafeGetElement(fieldExpMappingsQueue);
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

    private void createObjectMap(DynamicMessage.Builder messageBuilder, String fieldName, Integer mapSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        List<Message> messageMap = new ArrayList<>();
        Descriptors.FieldDescriptor descriptor = getFieldDescriptorForField(messageBuilder, fieldName);

        for (int i = 0; i < mapSize - 1; i++) {
            messageMap.add(buildMapEntry(descriptor, fieldName, fieldExpMappingsQueue.clone()));
        }

        messageMap.add(buildMapEntry(descriptor, fieldName, fieldExpMappingsQueue));
        messageBuilder.setField(descriptor, messageMap);
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
