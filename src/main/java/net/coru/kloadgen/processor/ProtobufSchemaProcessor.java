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
import java.util.List;
import java.util.Objects;


public class ProtobufSchemaProcessor{

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
        Object a = null;
        String b = schema.toSchema();
        System.out.println(b);
        ProtobufSchema protobufSchema = new ProtobufSchema(b);
        System.out.println(protobufSchema);
        DynamicMessage message = DynamicMessage.newBuilder(protobufSchema.toDescriptor()).build();
        System.out.println(message);
        message.toBuilder().setField(message.getDescriptorForType().findFieldByName("fieldXyz"), "a") ;


        if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
            ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
            FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
            while (!fieldExpMappingsQueue.isEmpty()) {

            }
        }

        return new EnrichedRecord(metadata, message.getAllFields());
    }



}
