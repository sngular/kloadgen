package net.coru.kloadgen.processor;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.squareup.wire.schema.internal.parser.MessageElement;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.generator.ProtoBufGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.util.ProtobufHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ProtobufSchemaProcessor extends SchemaProcessorLib {



    private ProtoFileElement schema;
    private SchemaMetadata metadata;
    private RandomObject randomObject;
    private RandomMap randomMap;
    private List<FieldValueMapping> fieldExprMappings;
    private ProtoBufGeneratorTool generatorTool;


    public void processSchema(ProtoFileElement schema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
        this.schema = schema;
        this.fieldExprMappings = fieldExprMappings;
        this.metadata = metadata;
        randomObject = new RandomObject();
        generatorTool = new ProtoBufGeneratorTool();
    }

    public void processSchema(ParsedSchema parsedSchema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
        this.schema = (ProtoFileElement) parsedSchema.rawSchema();
        this.fieldExprMappings = fieldExprMappings;
        this.metadata = metadata;
        randomObject = new RandomObject();
        generatorTool = new ProtoBufGeneratorTool();
        randomMap = new RandomMap();
    }

    public EnrichedRecord next() throws DescriptorValidationException, IOException {
        // ProtobufSchema protobufSchema = getProtobufSchema();
        DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(buildSomething());
        DynamicMessage message;
        if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
            ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
            FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

            while (!fieldExpMappingsQueue.isEmpty()) {
                fieldValueMapping.setFieldName(fieldValueMapping.getFieldName().substring(fieldValueMapping.getFieldName().indexOf(".") + 1));
                String cleanPath = cleanUpPath(fieldValueMapping, "");
                String typeName = getCleanMethodName(fieldValueMapping, "");
                String fieldName = fieldValueMapping.getFieldName().substring(fieldValueMapping.getFieldName().lastIndexOf(".") + 1);
                String typeFilter = cleanPath.replaceAll(typeName, "");
                if (typeFilter.matches("\\[.*]\\[.*")) {
                    if (checkIfArrayMap(Objects.requireNonNull(fieldValueMapping).getFieldType())) {
                        fieldValueMapping = processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, messageBuilder, typeName);
                    } else if (checkIfMapArray(fieldValueMapping.getFieldType())) {
                        fieldValueMapping = processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, messageBuilder, typeName);
                    } else if (checkIfIsRecordMapArray(cleanPath)) {
                        fieldValueMapping = processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, messageBuilder, typeName);
                    } else if (checkIfIsRecordArrayMap(cleanPath)) {
                        fieldValueMapping = processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, messageBuilder, typeName);
                    } else {
                        throw new KLoadGenException("Wrong configuration Map - Array");
                    }
                } else if (typeFilter.startsWith("[")) {
                    if (checkIfMap(typeFilter)) {
                        fieldValueMapping = processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, messageBuilder, typeName);
                    } else if (checkIfArray(typeFilter)) {
                        String cleanFieldName = fieldName.substring(0, fieldName.indexOf("["));
                        fieldValueMapping = processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, messageBuilder, typeName, cleanFieldName);
                    } else if (checkIfRecordMap(typeFilter)) {
                        fieldValueMapping = processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, messageBuilder, typeName);
                    } else if (checkIfRecordArray(typeFilter)) {
                        fieldValueMapping = processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, messageBuilder, typeName);
                    } else {
                        throw new KLoadGenException("Wrong configuration Map - Array");
                    }
                } else if (typeFilter.startsWith(".")) {
                    messageBuilder.setField(getFieldDescriptorForField(messageBuilder, typeName), createObject(getDescriptorForField(messageBuilder, typeName), typeName, fieldExpMappingsQueue));
                    fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
                } else {
                    if (fieldValueMapping.getFieldType().equals("enum")) {
                        processFieldValueMappingAsEnum(messageBuilder, fieldValueMapping, typeName, fieldName);
                    } else {
                        messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(typeName),
                                randomObject.generateRandom(
                                        fieldValueMapping.getFieldType(),
                                        fieldValueMapping.getValueLength(),
                                        fieldValueMapping.getFieldValuesList(),
                                        fieldValueMapping.getConstrains()
                                )
                        );
                    }
                    fieldExpMappingsQueue.remove();
                    fieldValueMapping = fieldExpMappingsQueue.peek();
                }
            }
        }
        message = messageBuilder.build();
        return new EnrichedRecord(metadata, message);
    }

    private Descriptors.Descriptor buildSomething() throws Descriptors.DescriptorValidationException, IOException {

        List<String> imports = schema.getImports();

        DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
        List<String> lines = null;
        for (String importedClass : imports) {
            String schemaToString = new String(getClass().getClassLoader().getResourceAsStream(importedClass).readAllBytes());
            lines = new ArrayList(CollectionUtils.select(Arrays.asList(schemaToString.split("\\n")), isValid()));
            if (!ProtobufHelper.NOT_ACCEPTED_IMPORTS.contains(importedClass)) {
                var importedSchema = processImported(lines);
                schemaBuilder.addDependency(importedSchema.getFileDescriptorSet().getFile(0).getName());
                schemaBuilder.addSchema(importedSchema);
            }
        }
        MessageElement messageElement = (MessageElement) schema.getTypes().get(0);
        schemaBuilder.addMessageDefinition(buildMessageDefinition(messageElement));
        DynamicSchema schema = schemaBuilder.build();
        System.out.println(schema);
        return schemaBuilder.build().getMessageDescriptor(messageElement.getName());
    }

    private MessageDefinition buildMessageDefinition(MessageElement messageElement) {
        HashMap<String, MessageElement> nestedTypes = new HashMap<>();
        fillNestedTypes(messageElement, nestedTypes);
        MessageDefinition.Builder msgDef = MessageDefinition.newBuilder(messageElement.getName());
        for (int i = 0; i < messageElement.getFields().size(); i++) {
            var messageType = messageElement.getFields().get(i).getType();

            var dotType = checkDotType(messageType);
            if (nestedTypes.containsKey(messageElement.getFields().get(i).getType())) {
                msgDef.addMessageDefinition(buildMessageDefinition(nestedTypes.get(messageElement.getFields().get(i).getType())));
            }
            if(nestedTypes.containsKey(dotType)){
                msgDef.addMessageDefinition(buildMessageDefinition(nestedTypes.get(dotType)));
            }
            if (messageElement.getFields().get(i).getType().startsWith("map")) {
                var realType = StringUtils.chop(messageType.substring(messageType.indexOf(',')+1).trim());
                var mapDotType = checkDotType(realType);
                if(nestedTypes.containsKey(realType)){
                    msgDef.addMessageDefinition(buildMessageDefinition(nestedTypes.get(realType)));
                }
                if(nestedTypes.containsKey(mapDotType)){
                    msgDef.addMessageDefinition(buildMessageDefinition(nestedTypes.get(mapDotType)));
                }
                msgDef.addField("repeated",
                        "typemapnumber" + i,
                        messageElement.getFields().get(i).getName(),
                        messageElement.getFields().get(i).getTag());

                msgDef.addMessageDefinition(
                        MessageDefinition.newBuilder("typemapnumber" + i)
                                .addField("optional",
                                        "string", "key", 1)
                                .addField("optional",
                                        realType, "value", 2).build()
                );
            } else {
                if (Objects.nonNull(messageElement.getFields().get(i).getLabel())) {
                    msgDef.addField(messageElement.getFields().get(i).getLabel().toString().toLowerCase(),
                            messageElement.getFields().get(i).getType(),
                            messageElement.getFields().get(i).getName(),
                            messageElement.getFields().get(i).getTag());
                } else {
                    msgDef.addField("optional",
                            messageElement.getFields().get(i).getType(),
                            messageElement.getFields().get(i).getName(),
                            messageElement.getFields().get(i).getTag());
                }

            }

        }
        return msgDef.build();
    }

    private void fillNestedTypes(MessageElement messageElement, HashMap<String, MessageElement> nestedTypes) {
        if (!messageElement.getNestedTypes().isEmpty()) {
            messageElement.getNestedTypes().forEach(nestedType ->
                    nestedTypes.put(nestedType.getName(), (MessageElement) nestedType)
            );
        }
    }


    private Predicate<String> isValid() {
        return line -> !line.contains("//") && !line.isEmpty();
    }

    private DynamicSchema processImported(List<String> importedLines) throws DescriptorValidationException {

        DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();

        var linesIterator = importedLines.listIterator();
        String packageName = "";
        while (linesIterator.hasNext()) {
            var fileLine = linesIterator.next();

            if (fileLine.startsWith("package")) {
                packageName = StringUtils.chop(fileLine.substring(7).trim());
                schemaBuilder.setPackage(packageName);
            }
            if (fileLine.startsWith("message")) {
                var messageName = StringUtils.chop(fileLine.substring(7).trim()).trim();
                schemaBuilder.setName(packageName + "." + messageName);

                    schemaBuilder.addMessageDefinition(buildMessage(messageName, linesIterator));

            }
            if (fileLine.startsWith("import")) {
                schemaBuilder.addDependency(fileLine.substring(6));
            }
        }

        return schemaBuilder.build();
    }

    private MessageDefinition buildMessage(String messageName, ListIterator<String> messageLines) {

        MessageDefinition.Builder messageDefinition = MessageDefinition.newBuilder(messageName);
        while (messageLines.hasNext()) {
            var field = messageLines.next().trim().split("\\s");
            if (ProtobufHelper.PROTOBUF_TYPES.containsKey(field[0])) {
                messageDefinition.addField("optional", field[0], field[1], Integer.parseInt(checkIfChoppable(field[3])));
            } else if (ProtobufHelper.LABEL.contains(field[0])) {
                messageDefinition.addField(field[0], field[1], field[2], Integer.parseInt(checkIfChoppable(field[4])));
            }
        }

        return messageDefinition.build();
    }

    public String checkIfChoppable(String field){
        String choppedField = field;
        if (field.endsWith(";")){
            choppedField = StringUtils.chop(field);
        }
        return choppedField;
    }

    private void processFieldValueMappingAsEnum(DynamicMessage.Builder messageBuilder, FieldValueMapping fieldValueMapping, String typeName, String fieldName) {
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
        Descriptors.EnumDescriptor enumDescriptor = getFieldDescriptorForField(messageBuilder, typeName).getEnumType();
        messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(fieldName),
                generatorTool.generateObject(enumDescriptor, fieldValueMapping.getFieldType(), arraySize, fieldValueMapping.getFieldValuesList()));
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
            String typeName = getCleanMethodName(fieldValueMapping, fieldName);
            String typeFilter = cleanFieldName.replaceAll(typeName, "");
            if (typeFilter.matches("\\[.]\\[.*") && !fieldValueMapping.getFieldType().endsWith("map-map") && !fieldValueMapping.getFieldType().endsWith("array-array")) {
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
            } else if (typeFilter.startsWith("[")) {
                if (checkIfMap(typeFilter)) {
                    String fieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if (checkIfArray(typeFilter)) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, messageBuilder, fieldName, fieldNameSubEntity);
                } else if (checkIfRecordMap(typeFilter)) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                } else if (checkIfRecordArray(typeFilter)) {
                    String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                    processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, messageBuilder, fieldNameSubEntity);
                }
            } else if (typeFilter.startsWith(".")) {
                String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, fieldName);
                messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(cleanFieldName),
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
        Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
        createObjectArray(messageBuilder,
                fieldName,
                arraySize,
                fieldExpMappingsQueue);
        fieldExpMappingsQueue.remove();
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsRecordMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
        messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(fieldName), createObjectMap(messageBuilder,
                fieldName,
                mapSize,
                fieldExpMappingsQueue));
        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsSimpleArray(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String typeName, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        if (Objects.nonNull(messageBuilder.getDescriptorForType().findFieldByName(typeName)) && messageBuilder.getDescriptorForType().findFieldByName(typeName).getType().equals(Descriptors.FieldDescriptor.Type.MESSAGE)) {
            messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(typeName), createObject(messageBuilder.getDescriptorForType().findFieldByName(typeName).getMessageType(), typeName, fieldExpMappingsQueue));
        } else if (Objects.nonNull(messageBuilder.getDescriptorForType().findFieldByName(typeName)) && messageBuilder.getDescriptorForType().findFieldByName(typeName).getType().equals(Descriptors.FieldDescriptor.Type.ENUM)) {
            Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
            Descriptors.EnumDescriptor enumDescriptor = getFieldDescriptorForField(messageBuilder, typeName).getEnumType();
            messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(typeName),
                    generatorTool.generateObject(enumDescriptor, fieldValueMapping.getFieldType(), arraySize, fieldValueMapping.getFieldValuesList()));
            fieldExpMappingsQueue.remove();
        } else {
            Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
            messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(fieldName),
                    createArray(fieldName, arraySize, fieldExpMappingsQueue));
        }

        return getSafeGetElement(fieldExpMappingsQueue);
    }

    private FieldValueMapping processFieldValueMappingAsSimpleMap(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, DynamicMessage.Builder messageBuilder, String fieldName) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), getCleanMethodName(fieldValueMapping, fieldName));
        messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName),
                createSimpleObjectMap(messageBuilder, fieldName, mapSize, fieldExpMappingsQueue)
        );
        fieldExpMappingsQueue.remove();
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

    private List<Message> createSimpleObjectMap(DynamicMessage.Builder messageBuilder, String fieldName, Integer mapSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        List<Message> messageMap = new ArrayList<>();
        Descriptors.FieldDescriptor descriptor = getFieldDescriptorForField(messageBuilder, fieldName);

        for (int i = 0; i < mapSize - 1; i++) {
            messageMap.add(buildSimpleMapEntry(descriptor, fieldName, fieldExpMappingsQueue.clone()));
        }
        messageMap.add(buildSimpleMapEntry(descriptor, fieldName, fieldExpMappingsQueue));
        messageBuilder.setField(descriptor, messageMap);
        return messageMap;
    }

    private Message buildMapEntry(Descriptors.FieldDescriptor descriptor, String fieldName, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor.getMessageType());
        Descriptors.FieldDescriptor keyFieldDescriptor = descriptor.getMessageType().findFieldByName("key");
        builder.setField(keyFieldDescriptor,
                randomObject.generateRandom("string", 10, Collections.emptyList(), Collections.emptyMap()));
        Descriptors.FieldDescriptor valueFieldDescriptor = descriptor.getMessageType().findFieldByName("value");
        if (valueFieldDescriptor.getType().equals(Descriptors.FieldDescriptor.Type.ENUM)) {
            List<String> fieldValueMappings = new ArrayList<>();
            for (Descriptors.EnumValueDescriptor value : valueFieldDescriptor.getEnumType().getValues()) {
                fieldValueMappings.add(value.getName());
            }
            builder.setField(valueFieldDescriptor, generatorTool.generateObject(valueFieldDescriptor.getEnumType(), valueFieldDescriptor.getType().name(), 0, fieldValueMappings));
        } else {
            builder.setField(valueFieldDescriptor,
                    createObject(valueFieldDescriptor.getMessageType(), fieldName, fieldExpMappingsQueue));
        }

        return builder.build();
    }

    private Message buildSimpleMapEntry(Descriptors.FieldDescriptor descriptor, String fieldName, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
        FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
        String fieldValueMappingCleanType = fieldValueMapping.getFieldType().substring(0, fieldValueMapping.getFieldType().indexOf("-map"));
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor.getMessageType());
        Descriptors.FieldDescriptor keyFieldDescriptor = descriptor.getMessageType().findFieldByName("key");
        builder.setField(keyFieldDescriptor,
                randomObject.generateRandom("string", 10, Collections.emptyList(), Collections.emptyMap()));
        Descriptors.FieldDescriptor valueFieldDescriptor = descriptor.getMessageType().findFieldByName("value");
        if (valueFieldDescriptor.getType().equals(Descriptors.FieldDescriptor.Type.ENUM)) {
            List<String> fieldValueMappings = new ArrayList<>();
            for (Descriptors.EnumValueDescriptor value : valueFieldDescriptor.getEnumType().getValues()) {
                fieldValueMappings.add(value.getName());
            }
            builder.setField(valueFieldDescriptor, generatorTool.generateObject(valueFieldDescriptor.getEnumType(), valueFieldDescriptor.getType().name(), 0, fieldValueMappings));
        } else {
            builder.setField(valueFieldDescriptor,
                    randomObject.generateRandom(
                            fieldValueMappingCleanType,
                            fieldValueMapping.getValueLength(),
                            fieldValueMapping.getFieldValuesList(),
                            fieldValueMapping.getConstrains()));
        }

        return builder.build();
    }

    private Descriptors.Descriptor getDescriptorForField(DynamicMessage.Builder messageBuilder, String typeName) {
        return messageBuilder.getDescriptorForType().findFieldByName(typeName).getMessageType();
    }

    private Descriptors.FieldDescriptor getFieldDescriptorForField(DynamicMessage.Builder messageBuilder, String typeName) {
        return messageBuilder.getDescriptorForType().findFieldByName(typeName);
    }
    private String checkDotType(String subfieldType) {
        String dotType = "";
        if (subfieldType.startsWith(".") || subfieldType.contains(".")) {
            String[] typeSplit = subfieldType.split("\\.");
            dotType = typeSplit[typeSplit.length - 1];
        }
        return dotType;
    }
}
