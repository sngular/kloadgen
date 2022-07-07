package net.coru.kloadgen.processor.objectcreator.impl;

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import net.coru.kloadgen.processor.ProtoBufProcessorHelper;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;
import net.coru.kloadgen.randomtool.generator.ProtoBufGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.commons.lang3.StringUtils;

public class ProtobufObjectCreatorFactory implements ObjectCreator {

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  private static final ProtoBufGeneratorTool GENERATOR_TOOL = new ProtoBufGeneratorTool();

  private Descriptors.Descriptor schema;

  private SchemaMetadata metadata;

  private Map<String, DynamicMessage.Builder> entity = new HashMap<>();

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
    DynamicMessage.Builder messageBuilder = entity.get(pojo.getRootFieldName());
    FieldDescriptor fieldDescriptor = findFieldDescriptor(pojo.getFieldNameSubEntity(), this.schema, new AtomicBoolean(false));
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
      final SchemaProcessorPOJO pojo,
      final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    DynamicMessage.Builder messageBuilder = entity.get(pojo.getRootFieldName());
    FieldDescriptor fieldDescriptor = findFieldDescriptor(pojo.getFieldNameSubEntity(), this.schema, new AtomicBoolean(false));
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

    var descriptor = findFieldDescriptor(pojo.getFieldNameSubEntity(), this.schema, new AtomicBoolean(false));
    Object object;

    if (MESSAGE.equals(descriptor.getType())) {
      object = createFieldObject(descriptor.getMessageType(), pojo);
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

    object = assignObject(pojo.getRootFieldName(), object, descriptor);
    return object;
  }

  @Override
  public Object assignRecord(final String targetObjectName, final String fieldName, final String recordToAssign) {
    DynamicMessage.Builder builder = entity.get(targetObjectName);
    builder.setField(findFieldDescriptor(fieldName, this.schema, new AtomicBoolean(false)), entity.get(recordToAssign).build());
    return builder;
  }

  @Override
  public void createRecord(final String objectName) {
    if ("root".equalsIgnoreCase(objectName)) {
      entity.put("root", DynamicMessage.newBuilder(schema));
    } else {
      FieldDescriptor fieldDescriptor = findFieldDescriptor(objectName, schema, new AtomicBoolean(false));
      Descriptor descriptor = fieldDescriptor.getMessageType();
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
  public Object generateSubentityRecord(Object objectRecord) {
    if (objectRecord instanceof Builder) {
      objectRecord = ((Builder) objectRecord).build();
    }
    return objectRecord;
  }

  @Override
  public boolean isOptional(final String rootFieldName, final String fieldName) {
    return false;
  }

  private boolean isDescriptorOfMap(Descriptor descriptor) {
    return descriptor.getName().startsWith("typemap");
  }

  public Object assignObject(final String targetObjectName, final Object objectToAssign, final Descriptors.FieldDescriptor descriptor) {
    DynamicMessage.Builder builder = entity.get(targetObjectName);
    builder.setField(descriptor, objectToAssign);
    return builder;
  }

  private Object createFieldObject(Descriptors.Descriptor descriptor, SchemaProcessorPOJO pojo) {
    DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(descriptor);
    for (var field : descriptor.getFields()) {
      messageBuilder.setField(field,
                              RANDOM_OBJECT.generateRandom(
                                  getFieldType(field),
                                  pojo.getValueLength(),
                                  pojo.getFieldValuesList(),
                                  pojo.getConstraints()));
    }

    return messageBuilder.build();
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
    Object objectReturn;
    if (Objects.nonNull(fieldDescriptor) && FieldDescriptor.Type.ENUM.equals(fieldDescriptor.getType())) {
      final var enumDescriptor = fieldDescriptor.getEnumType();
      objectReturn = GENERATOR_TOOL.generateObject(enumDescriptor, getOneDimensionValueType(pojo.getValueType()), pojo.getFieldSize(), pojo.getFieldValuesList());
    } else {
      objectReturn = ProtoBufGeneratorTool.generateArray(pojo.getFieldNameSubEntity(), pojo.getValueType(), pojo.getFieldSize(), pojo.getValueLength(), pojo.getFieldValuesList());
    }
    return objectReturn;
  }

  private Descriptors.FieldDescriptor findFieldDescriptor(final String objectName, final Descriptor descriptor, AtomicBoolean found) {
    FieldDescriptor field = null;
    List<FieldDescriptor> alFieldDescriptor = descriptor.getFields();
    Iterator<FieldDescriptor> fieldsIterator = alFieldDescriptor.iterator();
    while (fieldsIterator.hasNext() && !found.get()) {
      field = fieldsIterator.next();
      /*int firstIndexSubstring = field.getFullName().indexOf(".") > 0 ? field.getFullName().lastIndexOf(".") + 1 : 0;
      int lastIndexSubstring = field.getFullName().length();
      String cleanFieldName = field.getFullName().substring(firstIndexSubstring, lastIndexSubstring);*/
      if (objectName.equalsIgnoreCase(field.getName())) {
        found.set(true);
      } else if (Type.MESSAGE.equals(field.getType())) {
        field = findFieldDescriptor(objectName, field.getMessageType(), found);
      }
    }
    return field;
  }

  private Descriptors.FieldDescriptor findFieldDescriptorFromFullName(final String fullObjectName, final Descriptor descriptor, AtomicBoolean found) {
    FieldDescriptor field = null;
    List<FieldDescriptor> alFieldDescriptor = descriptor.getFields();
    Iterator<FieldDescriptor> fieldsIterator = alFieldDescriptor.iterator();
    while (fieldsIterator.hasNext() && !found.get()) {
      field = fieldsIterator.next();
      if (fullObjectName.equalsIgnoreCase(field.getFullName())) {
        found.set(true);
      } else if (Type.MESSAGE.equals(field.getType())) {
        field = findFieldDescriptorFromFullName(fullObjectName, field.getMessageType(), found);
      }
    }
    return field;
  }

  private Descriptors.Descriptor findDescriptor(final String fullObjectName, final Descriptor descriptor, AtomicBoolean found) {
    Descriptor foundDescriptor = null;
    if (fullObjectName.equalsIgnoreCase(descriptor.getFullName())) {
      found.set(true);
      foundDescriptor = descriptor;
    }
    List<FieldDescriptor> alFieldDescriptor = descriptor.getFields();
    Iterator<FieldDescriptor> fieldsIterator = alFieldDescriptor.iterator();
    while (fieldsIterator.hasNext() && !found.get()) {
      FieldDescriptor field = fieldsIterator.next();
      if (Type.MESSAGE.equals(field.getType()) && null != field.getMessageType()) {
        foundDescriptor = findDescriptor(fullObjectName, field.getMessageType(), found);
      }
    }
    return foundDescriptor;
  }

  private Descriptors.Descriptor getDescriptorForField(final DynamicMessage.Builder messageBuilder, final String typeName) {
    return messageBuilder.getDescriptorForType().findFieldByName(typeName).getMessageType();
  }

  private Descriptors.FieldDescriptor getFieldDescriptorForField(final DynamicMessage.Builder messageBuilder, final String typeName) {
    return messageBuilder.getDescriptorForType().findFieldByName(typeName);
  }

  private Object createFinalMap(final FieldDescriptor fieldDescriptor, final SchemaProcessorPOJO pojo) {
    final List<Message> messageMap = new ArrayList<>();
    for (int i = 0; i < pojo.getFieldSize(); i++) {
      messageMap.add(buildSimpleMapEntry(fieldDescriptor, pojo));
    }
    return messageMap;
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
      builder.setField(valueFieldDescriptor, GENERATOR_TOOL.generateObject(valueFieldDescriptor.getEnumType(), valueFieldDescriptor.getType().name(),
                                                                           0, fieldValueMappings));
    } else {
      builder.setField(valueFieldDescriptor,
                       RANDOM_OBJECT.generateRandom(
                           fieldValueMappingCleanType,
                           pojo.getValueLength(),
                           pojo.getFieldValuesList(),
                           pojo.getConstraints()));
    }

    return builder.build();
  }

  private String getOneDimensionValueType(String completeValueType) {
    int numberOfHyphens = StringUtils.countMatches(completeValueType, "-");
    return numberOfHyphens > 1 ? completeValueType.substring(0, completeValueType.lastIndexOf("-")) : completeValueType;
  }

  private String getSimpleValueType(String completeValueType) {
    return completeValueType.substring(0, completeValueType.indexOf("-") > 0 ? completeValueType.indexOf("-") : completeValueType.length());
  }

  private String generateString(final Integer valueLength) {
    return String.valueOf(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()));
  }
}
