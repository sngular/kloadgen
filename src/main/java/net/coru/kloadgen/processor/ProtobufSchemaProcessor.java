package net.coru.kloadgen.processor;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.DynamicMessage.Builder;
import com.google.protobuf.Message;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.generator.ProtoBufGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;

@Slf4j
public class ProtobufSchemaProcessor {

  public static final String STRING_TYPE = "string";

  private Descriptors.Descriptor schema;

  private SchemaMetadata metadata;

  private RandomObject randomObject;

  private RandomMap randomMap;

  private List<FieldValueMapping> fieldExprMappings;

  private ProtoBufGeneratorTool generatorTool;

  public final void processSchema(final ProtoFileElement schema, final SchemaMetadata metadata, final List<FieldValueMapping> fieldExprMappings)
      throws DescriptorValidationException, IOException {
    this.schema = new ProtoBufProcessorHelper().buildDescriptor(schema);
    this.fieldExprMappings = fieldExprMappings;
    this.metadata = metadata;
    randomObject = new RandomObject();
    generatorTool = new ProtoBufGeneratorTool();
  }

  public final void processSchema(final ParsedSchema parsedSchema, final SchemaMetadata metadata, final List<FieldValueMapping> fieldExprMappings)
      throws DescriptorValidationException, IOException {
    this.schema = new ProtoBufProcessorHelper().buildDescriptor((ProtoFileElement) parsedSchema.rawSchema());
    this.fieldExprMappings = fieldExprMappings;
    this.metadata = metadata;
    randomObject = new RandomObject();
    generatorTool = new ProtoBufGeneratorTool();
    randomMap = new RandomMap();
  }

  public final EnrichedRecord next() {
    final DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(schema);

    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();

      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      int generatedProperties = 0;
      int elapsedProperties = 0;

      while (!fieldExpMappingsQueue.isEmpty()) {
        final String methodName = SchemaProcessorLib.cleanUpPath(fieldValueMapping, "");
        final String fieldName = SchemaProcessorLib.getCleanMethodName(fieldValueMapping, "");
        final String typeFilter = methodName.replaceAll(fieldName, "");
        final String fieldType = fieldValueMapping.getFieldType();

        if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName))
            && generatedProperties == elapsedProperties && generatedProperties > 0 && Boolean.TRUE.equals(fieldValueMapping.getAncestorRequired())) {
          fieldValueMapping.setRequired(true);
          final List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          fieldExpMappingsQueueCopy.poll();
        } else {
          generatedProperties = 0;
          elapsedProperties = 0;
          fieldExpMappingsQueueCopy.poll();
        }
        generatedProperties++;

        if (Boolean.TRUE.equals(isOptionalField(messageBuilder, fieldName) && !fieldValueMapping.getRequired()) && fieldValueMapping.getFieldValuesList().contains("null")) {
          elapsedProperties++;
          fieldExpMappingsQueue.remove();
        } else {

          if (typeFilter.matches("\\[.*]\\[.*") && !fieldType.endsWith("map-map") && !fieldType.endsWith("array-array")) {
            if (SchemaProcessorLib.checkIfIsRecordMapArray(methodName)) {
              processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (SchemaProcessorLib.checkIfIsRecordArrayMap(methodName)) {
              processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (SchemaProcessorLib.checkIfArrayMap(Objects.requireNonNull(fieldType))) {
              processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (SchemaProcessorLib.checkIfMapArray(fieldType)) {
              processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else {
              throw new KLoadGenException("Wrong configuration Map - Array");
            }
          } else if (typeFilter.startsWith("[")) {
            if (SchemaProcessorLib.checkIfRecordMap(typeFilter)) {
              processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (SchemaProcessorLib.checkIfRecordArray(typeFilter)) {
              processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (SchemaProcessorLib.checkIfMap(typeFilter, fieldType)) {
              processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (SchemaProcessorLib.checkIfArray(typeFilter, fieldType)) {
              processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, messageBuilder, "", fieldName);
            } else {
              throw new KLoadGenException("Wrong configuration Map - Array");
            }
          } else if (typeFilter.startsWith(".")) {
            final String fieldNameSubEntity = SchemaProcessorLib.getCleanMethodName(fieldValueMapping, "");
            messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName),
                                    createObject(getDescriptorForField(messageBuilder, fieldNameSubEntity), fieldNameSubEntity, fieldExpMappingsQueue));
          } else {
            fieldExpMappingsQueue.poll();
            generateObject(messageBuilder, fieldValueMapping, fieldName);
          }
        }
        fieldValueMapping = SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);

      }
    }
    return new EnrichedRecord(metadata, messageBuilder.build());
  }

  private boolean isOptionalField(final Builder messageBuilder, final String fieldName) {
    return messageBuilder.getDescriptorForType().findFieldByName(fieldName).isOptional();
  }

  private Object createFieldObject(final Descriptors.Descriptor descriptor, final FieldValueMapping fieldValueMapping) {
    final var messageBuilder = DynamicMessage.newBuilder(descriptor);
    for (var field : descriptor.getFields()) {
      messageBuilder.setField(field,
                              randomObject.generateRandom(
                                  field.getType().getJavaType().name(),
                                  fieldValueMapping.getValueLength(),
                                  fieldValueMapping.getFieldValuesList(),
                                  fieldValueMapping.getConstraints()));
    }

    return messageBuilder.build();
  }

  private DynamicMessage createObject(final Descriptors.Descriptor subMessageDescriptor, final String parentFieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

    final var messageBuilder = DynamicMessage.newBuilder(subMessageDescriptor);
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;

    while (!fieldExpMappingsQueue.isEmpty()
           && (Objects.requireNonNull(fieldValueMapping).getFieldName().matches(".*" + parentFieldName + "$")
               || fieldValueMapping.getFieldName().matches(parentFieldName + "\\..*")
               || fieldValueMapping.getFieldName().matches(".*" + parentFieldName + "\\[.*")
               || fieldValueMapping.getFieldName().matches(".*" + parentFieldName + "\\..*"))) {
      final String methodName = SchemaProcessorLib.cleanUpPath(fieldValueMapping, parentFieldName);
      final String fieldName = SchemaProcessorLib.getCleanMethodName(fieldValueMapping, parentFieldName);
      final String collectionTail = methodName.replaceAll(fieldName, "");
      final String fieldType = fieldValueMapping.getFieldType();

      generatedProperties++;

      if ((FieldDescriptor.Type.MESSAGE.equals(subMessageDescriptor.findFieldByName(fieldName).getType())
           || subMessageDescriptor.findFieldByName(fieldName).isRepeated()
           || subMessageDescriptor.findFieldByName(fieldName).isMapField()
               && isOptionalField(messageBuilder, fieldName))
          && fieldValueMapping.getFieldValuesList().contains("null")) {

        elapsedProperties++;
        final FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueue.remove();
        final FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if (Boolean.TRUE.equals((fieldExpMappingsQueue.peek() == null || !Objects.requireNonNull(nextField).getFieldName().contains(parentFieldName))
                                && Objects.requireNonNull(actualField).getAncestorRequired())
            && generatedProperties == elapsedProperties && generatedProperties > 0) {

          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);
          final List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          if (fieldExpMappingsQueue.peek() == null) {
            fieldExpMappingsQueue.add(fieldValueMapping);
          }
        } else {
          fieldValueMapping = nextField;
        }
      } else {

        if (collectionTail.matches("\\[.]\\[.*") && !fieldType.endsWith("map-map") && !fieldType.endsWith("array-array")) {
          if (SchemaProcessorLib.checkIfIsRecordMapArray(methodName)) {
            processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, messageBuilder, fieldName);
          } else if (SchemaProcessorLib.checkIfIsRecordArrayMap(methodName)) {
            processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, messageBuilder, fieldName);
          } else if (SchemaProcessorLib.checkIfMapArray(fieldType)) {
            processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, messageBuilder, fieldName);
          } else if (SchemaProcessorLib.checkIfArrayMap(fieldType)) {
            processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, messageBuilder, fieldName);
          }
        } else if (collectionTail.startsWith("[")) {
          if (SchemaProcessorLib.checkIfRecordMap(methodName)) {
            processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, messageBuilder, fieldName);
          } else if (SchemaProcessorLib.checkIfRecordArray(methodName)) {
            processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, messageBuilder, fieldName);
          } else if (SchemaProcessorLib.checkIfMap(collectionTail, fieldType)) {
            processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, messageBuilder, fieldName);
          } else if (SchemaProcessorLib.checkIfArray(collectionTail, fieldType)) {
            processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, messageBuilder, parentFieldName, fieldName);
          }
        } else if (collectionTail.startsWith(".")) {
          final String fieldNameSubEntity = SchemaProcessorLib.getCleanMethodName(fieldValueMapping, parentFieldName);
          messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(fieldName),
                                  createObject(getDescriptorForField(messageBuilder, fieldNameSubEntity),
                                               fieldNameSubEntity,
                                               fieldExpMappingsQueue));

        } else {
          fieldExpMappingsQueue.poll();
          generateObject(messageBuilder, fieldValueMapping, fieldName);
        }
        fieldValueMapping = SchemaProcessorLib.getSafeGetElement(fieldExpMappingsQueue);
      }
    }
    return messageBuilder.build();
  }

  private void generateObject(final Builder messageBuilder, final FieldValueMapping fieldValueMapping, final String fieldName) {
    final var descriptor = messageBuilder.getDescriptorForType().findFieldByName(fieldName);
    if (FieldDescriptor.Type.MESSAGE.equals(descriptor.getType())) {
      messageBuilder.setField(descriptor, createFieldObject(descriptor.getMessageType(), fieldValueMapping));
    } else if (FieldDescriptor.Type.ENUM.equals(descriptor.getType())) {
      messageBuilder.setField(descriptor,
                              generatorTool.generateObject(descriptor.getEnumType(),
                                                           fieldValueMapping.getFieldType(),
                                                           fieldValueMapping.getValueLength(),
                                                           fieldValueMapping.getFieldValuesList())
      );
    } else {
      messageBuilder.setField(descriptor,
                              generatorTool.generateObject(descriptor,
                                                           fieldValueMapping.getFieldType(),
                                                           fieldValueMapping.getValueLength(),
                                                           fieldValueMapping.getFieldValuesList(),
                                                           fieldValueMapping.getConstraints()
                              )
      );
    }
  }

  private void processFieldValueMappingAsRecordArray(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final DynamicMessage.Builder messageBuilder,
      final String fieldName) {
    final var fieldValueMapping = fieldExpMappingsQueue.element();
    final var arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    createObjectArray(messageBuilder, fieldName, arraySize, fieldExpMappingsQueue);
  }

  private void processFieldValueMappingAsRecordMap(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final DynamicMessage.Builder messageBuilder, final String fieldName) {
    final var fieldValueMapping = fieldExpMappingsQueue.element();
    final var mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    messageBuilder.setField(messageBuilder.getDescriptorForType().findFieldByName(fieldName), createObjectMap(messageBuilder,
                                                                                                              fieldName,
                                                                                                              mapSize,
                                                                                                              fieldExpMappingsQueue));
  }

  private void processFieldValueMappingAsSimpleArray(
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final DynamicMessage.Builder messageBuilder, final String typeName, final String fieldName) {
    final var fieldValueMapping = fieldExpMappingsQueue.element();
    final FieldDescriptor fieldDescriptor = messageBuilder.getDescriptorForType().findFieldByName(fieldName);
    if (Objects.nonNull(fieldDescriptor) && FieldDescriptor.Type.MESSAGE.equals(fieldDescriptor.getType())) {
      log.info(fieldName);
      messageBuilder.setField(fieldDescriptor, createObject(fieldDescriptor.getMessageType(), typeName, fieldExpMappingsQueue));
    } else if (Objects.nonNull(fieldDescriptor) && FieldDescriptor.Type.ENUM.equals(fieldDescriptor.getType())) {
      final var arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
      final var enumDescriptor = getFieldDescriptorForField(messageBuilder, fieldName).getEnumType();
      messageBuilder.setField(fieldDescriptor,
                              generatorTool.generateObject(enumDescriptor, fieldValueMapping.getFieldType(), arraySize, fieldValueMapping.getFieldValuesList()));
      fieldExpMappingsQueue.remove();
    } else {
      final var arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
      messageBuilder.setField(fieldDescriptor, SchemaProcessorLib.createArray(fieldName, arraySize, fieldExpMappingsQueue));
    }
  }

  private void processFieldValueMappingAsSimpleMap(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final DynamicMessage.Builder messageBuilder, final String fieldName) {
    final var fieldValueMapping = fieldExpMappingsQueue.element();
    final var mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), SchemaProcessorLib.getCleanMethodName(fieldValueMapping, fieldName));
    messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName),
                            createSimpleObjectMap(messageBuilder, fieldName, mapSize, fieldExpMappingsQueue)
    );
    fieldExpMappingsQueue.remove();
  }

  private void processFieldValueMappingAsSimpleArrayMap(
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final DynamicMessage.Builder messageBuilder, final String fieldName) {
    final var fieldValueMapping = fieldExpMappingsQueue.element();
    fieldExpMappingsQueue.remove();
    final var arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    final var mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    final var simpleTypeArrayMap = SchemaProcessorLib.createSimpleTypeArrayMap(fieldName, fieldValueMapping.getFieldType(), arraySize, mapSize, fieldValueMapping.getValueLength(),
                                                      fieldValueMapping.getFieldValuesList());
    messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), simpleTypeArrayMap);
  }

  private void processFieldValueMappingAsSimpleMapArray(
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final DynamicMessage.Builder messageBuilder, final String fieldName) {
    final var fieldValueMapping = fieldExpMappingsQueue.poll();
    final var arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    final var mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

    final var mapArray = randomMap.generateMap(fieldValueMapping.getFieldType(), mapSize, fieldValueMapping.getFieldValuesList(), fieldValueMapping.getValueLength(), arraySize,
                                         fieldValueMapping.getConstraints());

    messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), mapArray);
  }

  private void processFieldValueMappingAsRecordArrayMap(
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final DynamicMessage.Builder messageBuilder, final String fieldName) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    final Integer arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    final Integer mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

    final Map<String, List<DynamicMessage>> recordMapArray = new HashMap<>(mapSize);
    for (int i = 0; i < mapSize - 1; i++) {
      final ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      recordMapArray.put((String) randomObject.generateRandom(STRING_TYPE, fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                         createComplexObjectArray(messageBuilder, fieldName, arraySize, temporalQueue));
    }
    recordMapArray.put((String) randomObject.generateRandom(STRING_TYPE, fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                       createComplexObjectArray(messageBuilder, fieldName, arraySize, fieldExpMappingsQueue));
    messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), recordMapArray);
  }

  private void processFieldValueMappingAsRecordMapArray(
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final DynamicMessage.Builder messageBuilder, final String fieldName) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    final Integer arraySize = SchemaProcessorLib.calculateSize(fieldValueMapping.getFieldName(), fieldName);
    final Integer mapSize = SchemaProcessorLib.calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    final var recordArrayMap = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {

      recordArrayMap.add(createObjectMap(messageBuilder, fieldName, mapSize, fieldExpMappingsQueue.clone()));
    }
    recordArrayMap.add(createObjectMap(messageBuilder, fieldName, arraySize, fieldExpMappingsQueue));
    messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName), recordArrayMap);
  }

  private void createObjectArray(final DynamicMessage.Builder messageBuilder, final String fieldName, final Integer arraySize,
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {

    for (int i = 0; i < arraySize - 1; i++) {
      messageBuilder.addRepeatedField(getFieldDescriptorForField(messageBuilder, fieldName),
                                      createObject(getDescriptorForField(messageBuilder, fieldName), fieldName, fieldExpMappingsQueue.clone()));
    }
    messageBuilder.addRepeatedField(getFieldDescriptorForField(messageBuilder, fieldName),
                                    createObject(getDescriptorForField(messageBuilder, fieldName), fieldName, fieldExpMappingsQueue));
  }

  private List<DynamicMessage> createComplexObjectArray(
      final DynamicMessage.Builder messageBuilder, final String fieldName, final Integer arraySize, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    final List<DynamicMessage> objectArray = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      final ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(messageBuilder.getDescriptorForType(), fieldName, temporalQueue));
    }
    objectArray.add(createObject(messageBuilder.getDescriptorForType(), fieldName, fieldExpMappingsQueue));
    return objectArray;
  }

  private List<Message> createObjectMap(final DynamicMessage.Builder messageBuilder, final String fieldName, final Integer mapSize,
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    final List<Message> messageMap = new ArrayList<>();
    final Descriptors.FieldDescriptor descriptor = getFieldDescriptorForField(messageBuilder, fieldName);

    for (int i = 0; i < mapSize - 1; i++) {
      messageMap.add(buildMapEntry(descriptor, fieldName, fieldExpMappingsQueue.clone()));
    }
    messageMap.add(buildMapEntry(descriptor, fieldName, fieldExpMappingsQueue));
    messageBuilder.setField(descriptor, messageMap);
    return messageMap;
  }

  private List<Message> createSimpleObjectMap(final DynamicMessage.Builder messageBuilder, final String fieldName, final Integer mapSize,
      final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    final List<Message> messageMap = new ArrayList<>();
    final Descriptors.FieldDescriptor descriptor = getFieldDescriptorForField(messageBuilder, fieldName);

    for (int i = 0; i < mapSize - 1; i++) {
      messageMap.add(buildSimpleMapEntry(descriptor, fieldExpMappingsQueue.clone()));
    }
    messageMap.add(buildSimpleMapEntry(descriptor, fieldExpMappingsQueue));
    messageBuilder.setField(descriptor, messageMap);
    return messageMap;
  }

  private Message buildMapEntry(final Descriptors.FieldDescriptor descriptor, final String fieldName, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    final DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor.getMessageType());
    final Descriptors.FieldDescriptor keyFieldDescriptor = descriptor.getMessageType().findFieldByName("key");
    builder.setField(keyFieldDescriptor,
                     randomObject.generateRandom(STRING_TYPE, 10, Collections.emptyList(), Collections.emptyMap()));
    final Descriptors.FieldDescriptor valueFieldDescriptor = descriptor.getMessageType().findFieldByName("value");
    if (valueFieldDescriptor.getType().equals(FieldDescriptor.Type.ENUM)) {
      final List<String> fieldValueMappings = new ArrayList<>();
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

  private Message buildSimpleMapEntry(final Descriptors.FieldDescriptor descriptor, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    final FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    final String fieldValueMappingCleanType = fieldValueMapping.getFieldType().substring(0, fieldValueMapping.getFieldType().indexOf("-map"));
    final DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor.getMessageType());
    final Descriptors.FieldDescriptor keyFieldDescriptor = descriptor.getMessageType().findFieldByName("key");
    builder.setField(keyFieldDescriptor, randomObject.generateRandom(STRING_TYPE, 10, Collections.emptyList(), Collections.emptyMap()));
    final Descriptors.FieldDescriptor valueFieldDescriptor = descriptor.getMessageType().findFieldByName("value");
    if (valueFieldDescriptor.getType().equals(FieldDescriptor.Type.ENUM)) {
      final List<String> fieldValueMappings = new ArrayList<>();
      for (Descriptors.EnumValueDescriptor value : valueFieldDescriptor.getEnumType().getValues()) {
        fieldValueMappings.add(value.getName());
      }
      builder.setField(valueFieldDescriptor, generatorTool.generateObject(valueFieldDescriptor.getEnumType(), valueFieldDescriptor.getType().name(),
                                                                          0, fieldValueMappings));
    } else {
      builder.setField(valueFieldDescriptor,
                       randomObject.generateRandom(
                           fieldValueMappingCleanType,
                           fieldValueMapping.getValueLength(),
                           fieldValueMapping.getFieldValuesList(),
                           fieldValueMapping.getConstraints()));
    }

    return builder.build();
  }

  private Descriptors.Descriptor getDescriptorForField(final DynamicMessage.Builder messageBuilder, final String typeName) {
    return messageBuilder.getDescriptorForType().findFieldByName(typeName).getMessageType();
  }

  private Descriptors.FieldDescriptor getFieldDescriptorForField(final DynamicMessage.Builder messageBuilder, final String typeName) {
    return messageBuilder.getDescriptorForType().findFieldByName(typeName);
  }

}
