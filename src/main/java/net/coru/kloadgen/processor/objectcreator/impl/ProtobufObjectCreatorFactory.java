package net.coru.kloadgen.processor.objectcreator.impl;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.processor.ProtoBufProcessorHelper;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.GenerationFunctionPOJO;
import net.coru.kloadgen.randomtool.generator.ProtoBufGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import org.apache.avro.generic.GenericRecord;

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

public class ProtobufObjectCreatorFactory implements ObjectCreator {

  private Descriptors.Descriptor SCHEMA;

  private SchemaMetadata METADATA;

  private RandomObject RANDOM_OBJECT;

  private RandomMap RANDOM_MAP;

  private ProtoBufGeneratorTool GENERATOR_TOOL;

  private final Map<String, DynamicMessage> ENTITY = new HashMap<>();

  public ProtobufObjectCreatorFactory(Object SCHEMA, Object METADATA) throws DescriptorValidationException, IOException {
    if (SCHEMA instanceof ParsedSchema) {
      this.SCHEMA = new ProtoBufProcessorHelper().buildDescriptor((ProtoFileElement) ((ParsedSchema) SCHEMA).rawSchema());
    } else if (SCHEMA instanceof ProtoFileElement) {
      this.SCHEMA = new ProtoBufProcessorHelper().buildDescriptor((ProtoFileElement) SCHEMA);
    }
    this.METADATA = (SchemaMetadata) METADATA;
    this.RANDOM_OBJECT = new RandomObject();
    this.RANDOM_MAP = new RandomMap();
    this.GENERATOR_TOOL = new ProtoBufGeneratorTool();
  }

  @Override
  public String generateString(final Integer valueLength) {
    return String.valueOf(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()));
  }

  @Override
  public Object createMap(
      final String objectName, final ArrayDeque<?> fieldExpMappingsQueue, final String fieldName, final String completeFieldName, final Integer mapSize,
      final String completeTypeFilterChain, final String valueType,
      final Integer valueLength, final List<String> fieldValuesList, final Map<ConstraintTypeEnum, String> constraints, final int level, final BiFunction<ArrayDeque<?>,
                                                                                                                                                             GenerationFunctionPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createArray(
      final String objectName, final ArrayDeque<?> fieldExpMappingsQueue, final String fieldName, final String completeFieldName, final Integer arraySize,
      final String completeTypeFilterChain, final String valueType,
      final Integer valueLength, final List<String> fieldValuesList, final Map<ConstraintTypeEnum, String> constraints, final int level, final BiFunction<ArrayDeque<?>,
                                                                                                                                                             GenerationFunctionPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createRepeatedObject(
      final String fieldName, final String completeFieldName, final String valueType, final Integer valueLength, final List<String> fieldValuesList,
      Map<ConstraintTypeEnum, String> constraints) {

    //var descriptor = findSchema(fieldName);
    var descriptor = SCHEMA.findFieldByName(fieldName);
    Object object = null;

    //var descriptor = messageBuilder.getDescriptorForType().findFieldByName(fieldName);
    if (MESSAGE.equals(descriptor.getType())) {
      //messageBuilder.setField(descriptor, createFieldObject(descriptor.getMessageType(), fieldValueMapping));
      object = GENERATOR_TOOL.generateObject(descriptor, valueType, valueLength, fieldValuesList, constraints);
    } else if (ENUM.equals(descriptor.getType())) {
      object = GENERATOR_TOOL.generateObject(descriptor.getEnumType(),
                                             valueType,
                                             valueLength,
                                             fieldValuesList
      );
    } else {
      object = GENERATOR_TOOL.generateObject(descriptor,
                                             valueType,
                                             valueLength,
                                             fieldValuesList,
                                             constraints
      );
    }
    return object;
  }

  @Override
  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    DynamicMessage entityObject = ENTITY.get(targetObjectName);
    //entityObject.put(fieldName, objectToAssign);

    return entityObject;

  }

  @Override
  public Object assignRecord(final String targetObjectName, final String fieldName, final String recordToAssign) {
    return null;
  }

  @Override
  public Object createRecord(final String objectName) {
    return null;
  }

  @Override
  public Object generateRecord() {
    return null;
  }

  @Override
  public boolean isOptional(final String rootFieldName, final String fieldName) {
    return false;
  }
}
