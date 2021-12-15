package net.coru.kloadgen.processor;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import io.confluent.kafka.schemaregistry.protobuf.dynamic.DynamicSchema;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;


import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class ProtobufSchemaProcessor extends SchemaProcessorLib{

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
        randomMap = new RandomMap();

    }

    public void processSchema(ParsedSchema parsedSchema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
        this.schema = (ProtoFileElement) parsedSchema.rawSchema();
        this.fieldExprMappings = fieldExprMappings;
        this.metadata = metadata;
        randomObject = new RandomObject();
        randomMap = new RandomMap();
    }

    public EnrichedRecord next()  {
        String schemaToString = schema.toSchema();
        ProtobufSchema protobufSchema = new ProtobufSchema(schemaToString);
        DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(protobufSchema.toDescriptor());
        DynamicMessage message;
        if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
            ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
            FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
            while (!fieldExpMappingsQueue.isEmpty()) {
                String cleanPath = cleanUpPath(fieldValueMapping, "");
                String[] cleanPathSplitted = cleanPath.split("\\.");
                String fieldName = getCleanMethodName(fieldValueMapping, "");
                //TODO messageBuilder.getDescriptorForType().findFieldByName("addressesArray").getMessageType().getFields() PARA OBJETOS ENDEBIDOS USAMOS ESTO. IMPORTANTE!!!!!!!!!
                messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(cleanPathSplitted[cleanPathSplitted.length -1]),
                        randomObject.generateRandom(
                                fieldValueMapping.getFieldType(),
                                fieldValueMapping.getValueLength(),
                                fieldValueMapping.getFieldValuesList(),
                                fieldValueMapping.getConstrains()));
                fieldExpMappingsQueue.remove();
                fieldValueMapping = fieldExpMappingsQueue.peek();
            }
        }
        message = messageBuilder.build();
        return new EnrichedRecord(metadata, message.getAllFields());
    }



}
