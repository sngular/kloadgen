package net.coru.kloadgen.processor.objectcreator.impl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.DynamicMessage.Builder;
import com.google.protobuf.Message;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.processor.SchemaProcessorUtils;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;
import net.coru.kloadgen.randomtool.generator.ProtoBufGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.commons.lang3.StringUtils;

public class ProtobufObjectCreatorFactory implements ObjectCreator {

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  private static final ProtoBufGeneratorTool PROTOBUF_GENERATOR_TOOL = new ProtoBufGeneratorTool();

  private final Descriptors.Descriptor schema;

  private final SchemaMetadata metadata;

  private final Map<String, DynamicMessage.Builder> entity = new HashMap<>();

  public ProtobufObjectCreatorFactory(final Object schema, final Object metadata) throws DescriptorValidationException, IOException {
    if (schema instanceof ParsedSchema) {
      this.schema = SchemaProcessorUtils.buildProtoDescriptor((ProtoFileElement) ((ParsedSchema) schema).rawSchema());
    } else if (schema instanceof ProtoFileElement) {
      this.schema = SchemaProcessorUtils.buildProtoDescriptor((ProtoFileElement) schema);
    } else {
      throw new KLoadGenException("Unsupported schema type");
    }
    this.metadata = (SchemaMetadata) metadata;
  }

  @Override
  public Object createMap(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerMap) {
    final DynamicMessage.Builder messageBuilder = entity.get(pojo.getRootFieldName());
    final String subPathName = SchemaProcessorUtils.getPathUpToFieldName(pojo.getCompleteFieldName(), pojo.getLevel() + 1);
    final FieldDescriptor fieldDescriptor = findFieldDescriptor(SchemaProcessorUtils.splitAndNormalizeFullFieldName(subPathName), this.schema, new AtomicBoolean(false));
    if (pojo.isLastFilterTypeOfLastElement()) {
      messageBuilder.setField(fieldDescriptor, createFinalMap(fieldDescriptor, pojo));
    } else {
      final List<Message> messageMap = new ArrayList<>();
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        final DynamicMessage.Builder builder = DynamicMessage.newBuilder(fieldDescriptor.getMessageType());
        final Descriptors.FieldDescriptor keyFieldDescriptor = fieldDescriptor.getMessageType().findFieldByName("key");
        builder.setField(keyFieldDescriptor, generateString(pojo.getValueLength()));
        final Descriptors.FieldDescriptor valueFieldDescriptor = fieldDescriptor.getMessageType().findFieldByName("value");
        //pojo.setFieldNameSubEntity(valueFieldDescriptor.getFullName());
        if (i == pojo.getFieldSize() - 1) {
          builder.setField(valueFieldDescriptor, generateFunction.apply(pojo.getFieldExpMappingsQueue(), pojo));
        } else {
          builder.setField(valueFieldDescriptor, generateFunction.apply(pojo.getFieldExpMappingsQueue().clone(), pojo));
        }
        messageMap.add(builder.build());
      }
      messageBuilder.setField(fieldDescriptor, messageMap);
    }
    return messageBuilder.build();
  }

  @Override
  public Object createArray(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerArray) {
    final DynamicMessage.Builder messageBuilder = entity.get(pojo.getRootFieldName());
    final String subPathName = SchemaProcessorUtils.getPathUpToFieldName(pojo.getCompleteFieldName(), pojo.getLevel() + 1);
    final FieldDescriptor fieldDescriptor = findFieldDescriptor(SchemaProcessorUtils.splitAndNormalizeFullFieldName(subPathName), this.schema, new AtomicBoolean(false));
    if (pojo.isLastFilterTypeOfLastElement()) {
      messageBuilder.setField(fieldDescriptor, createFinalArray(fieldDescriptor, pojo));
    } else {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        if (i == pojo.getFieldSize() - 1) {
          messageBuilder.addRepeatedField(fieldDescriptor, generateFunction.apply(pojo.getFieldExpMappingsQueue(), pojo));
        } else {
          messageBuilder.addRepeatedField(fieldDescriptor, generateFunction.apply(pojo.getFieldExpMappingsQueue().clone(), pojo));
        }
      }
    }
    return messageBuilder.build();
  }

  @Override
  public Object createValueObject(
      final SchemaProcessorPOJO pojo) {

    final var descriptor = findFieldDescriptor(SchemaProcessorUtils.splitAndNormalizeFullFieldName(pojo.getCompleteFieldName()), this.schema, new AtomicBoolean(false));
    Object object;

    if (Type.MESSAGE.equals(descriptor.getType())) {
      object = createFieldObject(descriptor.getMessageType(), pojo);
    } else if (Type.ENUM.equals(descriptor.getType())) {
      object = PROTOBUF_GENERATOR_TOOL.generateObject(descriptor.getEnumType(), pojo.getValueType(), pojo.getValueLength(), pojo.getFieldValuesList());
    } else {
      object = PROTOBUF_GENERATOR_TOOL.generateObject(descriptor, pojo.getValueType(), pojo.getValueLength(), pojo.getFieldValuesList(), pojo.getConstraints());
    }

    object = assignObject(pojo.getRootFieldName(), object, descriptor);
    return object;
  }

  @Override
  public Object assignRecord(final SchemaProcessorPOJO pojo) {
    final DynamicMessage.Builder builder = entity.get(pojo.getRootFieldName());
    final String subPathName = SchemaProcessorUtils.getPathUpToFieldName(pojo.getCompleteFieldName(), pojo.getLevel() + 1);
    builder.setField(findFieldDescriptor(SchemaProcessorUtils.splitAndNormalizeFullFieldName(subPathName), this.schema, new AtomicBoolean(false)),
                     entity.get(pojo.getFieldNameSubEntity()).build());
    return builder;
  }

  @Override
  public void createRecord(final String objectName, final String completeFieldName) {
    if ("root".equalsIgnoreCase(objectName)) {
      entity.put("root", DynamicMessage.newBuilder(schema));
    } else {
      final FieldDescriptor fieldDescriptor = findFieldDescriptor(SchemaProcessorUtils.splitAndNormalizeFullFieldName(completeFieldName), schema, new AtomicBoolean(false));
      Descriptor descriptor = fieldDescriptor.getMessageType();
      //if(fieldDescriptor.isMapField())
      System.out.println("FieldDescriptorMap -> "+fieldDescriptor.isMapField());
      System.out.println("IsDescriptorofMap -> "+ isDescriptorOfMap(descriptor));
      if (isDescriptorOfMap(descriptor)) {
        descriptor = descriptor.getFields().get(1).getMessageType();
      }
      entity.put(objectName, DynamicMessage.newBuilder(descriptor));
    }
  }

  @Override
  public Object generateRecord() {
    return EnrichedRecord.builder().schemaMetadata(metadata).genericRecord(entity.get("root").build()).build();
  }

  @Override
  public Object generateSubEntityRecord(final Object objectRecord) {
    Object returnObject = objectRecord;
    if (objectRecord instanceof Builder) {
      returnObject = ((Builder) objectRecord).build();
    }
    return returnObject;
  }

  @Override
  public boolean isOptional(final SchemaProcessorPOJO pojo) {
    final String subPathName = SchemaProcessorUtils.getPathUpToFieldName(pojo.getCompleteFieldName(), pojo.getLevel() + 1);
    final FieldDescriptor fieldDescriptor = findFieldDescriptor(SchemaProcessorUtils.splitAndNormalizeFullFieldName(subPathName), this.schema, new AtomicBoolean(false));
    return Type.MESSAGE.equals(fieldDescriptor.getType()) || fieldDescriptor.isRepeated() || fieldDescriptor.isMapField() && fieldDescriptor.isOptional();
  }

  private boolean isDescriptorOfMap(final Descriptor descriptor) {
    return descriptor.getName().startsWith("typemap");
  }

  private Object createFieldObject(final Descriptors.Descriptor descriptor, final SchemaProcessorPOJO pojo) {
    final DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(descriptor);
    for (var field : descriptor.getFields()) {
      messageBuilder.setField(field, RANDOM_OBJECT.generateRandom(getFieldType(field), pojo.getValueLength(), pojo.getFieldValuesList(), pojo.getConstraints()));
    }

    return messageBuilder.build();
  }

  public Object assignObject(final String targetObjectName, final Object objectToAssign, final Descriptors.FieldDescriptor descriptor) {
    final DynamicMessage.Builder builder = entity.get(targetObjectName);
    builder.setField(descriptor, objectToAssign);
    return builder;
  }

  private String getFieldType(final Descriptors.FieldDescriptor field) {
    final String type;
    if (field.getFullName().endsWith("Date.year")) {
      type = "INT_YEAR";
    } else if (field.getFullName().endsWith("Date.month")) {
      type = "INT_MONTH";
    } else if (field.getFullName().endsWith("Date.day")) {
      type = "INT_DAY";
    } else if (field.getFullName().endsWith("TimeOfDay.hours")) {
      type = "INT_HOURS";
    } else if (field.getFullName().endsWith("TimeOfDay.minutes")) {
      type = "INT_MINUTES";
    } else if (field.getFullName().endsWith("TimeOfDay.seconds")) {
      type = "INT_SECONDS";
    } else if (field.getFullName().endsWith("TimeOfDay.nanos")) {
      type = "INT_NANOS";
    } else {
      type = field.getType().getJavaType().name();
    }
    return type;
  }

  private Object createFinalArray(final FieldDescriptor fieldDescriptor, final SchemaProcessorPOJO pojo) {
    final Object objectReturn;
    if (Objects.nonNull(fieldDescriptor) && FieldDescriptor.Type.ENUM.equals(fieldDescriptor.getType())) {
      final var enumDescriptor = fieldDescriptor.getEnumType();
      objectReturn = PROTOBUF_GENERATOR_TOOL.generateObject(enumDescriptor, getOneDimensionValueType(pojo.getValueType()), pojo.getFieldSize(), pojo.getFieldValuesList());
    } else {
      objectReturn = ProtoBufGeneratorTool.generateArray(pojo.getFieldNameSubEntity(), pojo.getValueType(), pojo.getFieldSize(), pojo.getValueLength(), pojo.getFieldValuesList());
    }
    return objectReturn;
  }

  private String getOneDimensionValueType(final String completeValueType) {
    final int numberOfHyphens = StringUtils.countMatches(completeValueType, "-");
    return numberOfHyphens > 1 ? completeValueType.substring(0, completeValueType.lastIndexOf("-")) : completeValueType;
  }

  private Descriptors.FieldDescriptor findFieldDescriptor(final String[] objectNameFields, final Descriptor descriptor, final AtomicBoolean found) {
    FieldDescriptor fieldDescriptor = null;

    if (null != objectNameFields && objectNameFields.length > 0) {
      final boolean isLastElement = objectNameFields.length == 1;
      if (isDescriptorOfMap(descriptor)) {
        fieldDescriptor = descriptor.getFields().get(1).getMessageType().findFieldByName(objectNameFields[0]);
      } else {
        fieldDescriptor = descriptor.findFieldByName(objectNameFields[0]);
      }
      found.set(fieldDescriptor.getName().equalsIgnoreCase(objectNameFields[0]) && isLastElement);
      if (!found.get()) {
        final Descriptor newDescriptor = fieldDescriptor.getMessageType();
        fieldDescriptor = findFieldDescriptor(Arrays.copyOfRange(objectNameFields, 1, objectNameFields.length), newDescriptor, found);
      }
    }

    return fieldDescriptor;
  }

  private Object createFinalMap(final FieldDescriptor fieldDescriptor, final SchemaProcessorPOJO pojo) {
    final List<Message> messageMap = new ArrayList<>();
    for (int i = 0; i < pojo.getFieldSize(); i++) {
      messageMap.add(buildSimpleMapEntry(fieldDescriptor, pojo));
    }
    return messageMap;
  }

  private String generateString(final Integer valueLength) {
    return String.valueOf(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()));
  }

  private Message buildSimpleMapEntry(final Descriptors.FieldDescriptor descriptor, final SchemaProcessorPOJO pojo) {
    final String fieldValueMappingCleanType = getSimpleValueType(pojo.getValueType());
    final DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor.getMessageType());
    final Descriptors.FieldDescriptor keyFieldDescriptor = descriptor.getMessageType().findFieldByName("key");
    builder.setField(keyFieldDescriptor, generateString(pojo.getValueLength()));
    final Descriptors.FieldDescriptor valueFieldDescriptor = descriptor.getMessageType().findFieldByName("value");
    if (valueFieldDescriptor.getType().equals(FieldDescriptor.Type.ENUM)) {
      final List<String> fieldValueMappings = new ArrayList<>();
      for (Descriptors.EnumValueDescriptor value : valueFieldDescriptor.getEnumType().getValues()) {
        fieldValueMappings.add(value.getName());
      }
      builder.setField(valueFieldDescriptor,
                       PROTOBUF_GENERATOR_TOOL.generateObject(valueFieldDescriptor.getEnumType(), valueFieldDescriptor.getType().name(), 0, fieldValueMappings));
    } else {
      builder.setField(valueFieldDescriptor, RANDOM_OBJECT.generateRandom(fieldValueMappingCleanType, pojo.getValueLength(), pojo.getFieldValuesList(), pojo.getConstraints()));
    }

    return builder.build();
  }

  private String getSimpleValueType(final String completeValueType) {
    return completeValueType.substring(0, completeValueType.indexOf("-") > 0 ? completeValueType.indexOf("-") : completeValueType.length());
  }
}
