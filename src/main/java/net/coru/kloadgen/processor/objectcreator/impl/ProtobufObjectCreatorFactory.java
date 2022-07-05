package net.coru.kloadgen.processor.objectcreator.impl;

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.DynamicMessage.Builder;
import com.google.protobuf.Message;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.ProtoBufProcessorHelper;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;
import net.coru.kloadgen.randomtool.generator.ProtoBufGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;

public class ProtobufObjectCreatorFactory implements ObjectCreator {

  private Descriptors.Descriptor schema;

  private SchemaMetadata metadata;

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  private static final RandomMap RANDOM_MAP = new RandomMap();

  private static final ProtoBufGeneratorTool GENERATOR_TOOL = new ProtoBufGeneratorTool();

  private DynamicMessage.Builder entityBuilder;

  private Map<String, Object> structure = new HashMap<>();

  public ProtobufObjectCreatorFactory(Object schema, Object metadata) throws DescriptorValidationException, IOException {
    if (schema instanceof ParsedSchema) {
      this.schema = new ProtoBufProcessorHelper().buildDescriptor((ProtoFileElement) ((ParsedSchema) schema).rawSchema());
    } else if (schema instanceof ProtoFileElement) {
      this.schema = new ProtoBufProcessorHelper().buildDescriptor((ProtoFileElement) schema);
    }
    this.metadata = (SchemaMetadata) metadata;
  }

  @Override
  public Object createMap(
      final SchemaProcessorPOJO pojo,
      final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createArray(
      final SchemaProcessorPOJO pojo,
      final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createValueObject(
      final SchemaProcessorPOJO pojo) {

    //var descriptor = findSchema(fieldName);
    var descriptor = schema.findFieldByName(pojo.getFieldNameSubEntity());
    Object object = null;

    //var descriptor = messageBuilder.getDescriptorForType().findFieldByName(fieldName);
    if (MESSAGE.equals(descriptor.getType())) {
      object = createFieldObject(descriptor.getMessageType(), pojo);
     // object = GENERATOR_TOOL.generateObject(descriptor, pojo.getValueType(), pojo.getValueLength(), pojo.getFieldValuesList(), pojo.getConstraints());
    } else if (ENUM.equals(descriptor.getType())) {
      object = GENERATOR_TOOL.generateObject(descriptor.getEnumType(),
                                             pojo.getValueType(),
                                             pojo.getValueLength(),
                                             pojo.getFieldValuesList()
      );
    } else {
      object = GENERATOR_TOOL.generateObject(descriptor,
                                             pojo.getValueType(),
                                             pojo.getValueLength(),
                                             pojo.getFieldValuesList(),
                                             pojo.getConstraints()
      );
    }

    object = assignObject(pojo.getRootFieldName(), pojo.getFieldNameSubEntity(), object, descriptor);
    return object;
  }

  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign, final Descriptors.FieldDescriptor descriptor) {
    Message message = (Message) structure.get(targetObjectName);
    message =  message.toBuilder().setField(descriptor, objectToAssign).build();
    structure.put(fieldName, message);
    return message;
  }

  @Override
  public Object assignRecord(final String targetObjectName, final String fieldName, final String recordToAssign) {
    return null;
  }

  @Override
  public void createRecord(final String objectName) {

    if ("root".equalsIgnoreCase(objectName)) {
      entityBuilder = DynamicMessage.newBuilder(schema);
    } else{

    }

  }

  @Override
  public Object generateRecord() {
    return null;
  }

  @Override
  public boolean isOptional(final String rootFieldName, final String fieldName) {
    return false;
  }

  private String generateString(final Integer valueLength) {
    return String.valueOf(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()));
  }

  private Object createFieldObject(Descriptors.Descriptor descriptor ,SchemaProcessorPOJO pojo) {
    DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(descriptor);
    for (var field : descriptor.getFields()) {
      messageBuilder.setField(field,
                              RANDOM_OBJECT.generateRandom(
                                  pojo.getValueType(),
                                  pojo.getValueLength(),
                                  pojo.getFieldValuesList(),
                                  pojo.getConstraints()));
    }

    return messageBuilder.build();
  }

}
