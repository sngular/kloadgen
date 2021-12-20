package net.coru.kloadgen.processor;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.TypeElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


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
                String[] cleanPathSplitted = cleanPath.split("\\.");
                String fieldName = cleanPathSplitted[cleanPathSplitted.length - (cleanPathSplitted.length-2)];
                String typeName = cleanPathSplitted[cleanPathSplitted.length - (cleanPathSplitted.length-1)];
                if (messageBuilder.getDescriptorForType().findFieldByName(typeName) != null){
                    Descriptors.Descriptor subDescriptor = messageBuilder.getDescriptorForType().findFieldByName(typeName).getMessageType();
                    if (nestedTypes.containsKey(subDescriptor.getName())) {
                        DynamicMessage.Builder subMessageBuilder = DynamicMessage.newBuilder(subDescriptor);
                        messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(typeName),  createObject(subMessageBuilder, fieldValueMapping, fieldName));
                    }
                } else {
                    processSimpleTypes(messageBuilder, fieldValueMapping, fieldName);
                }
                fieldExpMappingsQueue.remove();
                fieldValueMapping = fieldExpMappingsQueue.peek();
            }
        }

        message = messageBuilder.build();
        return new EnrichedRecord(metadata, message.getAllFields());
    }

    private DynamicMessage createObject(DynamicMessage.Builder subMessageBuilder, FieldValueMapping fieldValueMapping, String fieldName) {

        if (fieldName.endsWith("[]")) {
            processArray(subMessageBuilder, fieldValueMapping, fieldName);
        } else {
            subMessageBuilder.setField(subMessageBuilder.getDescriptorForType().findFieldByName(fieldName),
                    randomObject.generateRandom(
                            fieldValueMapping.getFieldType(),
                            fieldValueMapping.getValueLength(),
                            fieldValueMapping.getFieldValuesList(),
                            fieldValueMapping.getConstrains()));

        }
        return subMessageBuilder.build();
    }

    @NotNull
    private ProtobufSchema getProtobufSchema() {
        String schemaToString = schema.toSchema();
        return new ProtobufSchema(schemaToString);
    }

    private void processSimpleTypes(DynamicMessage.Builder messageBuilder, FieldValueMapping fieldValueMapping, String fieldName) {
        if (fieldName.endsWith("[]")) {
            processArray(messageBuilder, fieldValueMapping, fieldName);
        } else {
            messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(fieldName),
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
            messageBuilder.addRepeatedField(messageBuilder.getDescriptorForType().findFieldByName(arrayCleanPath),
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
}
