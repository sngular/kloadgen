package net.coru.kloadgen.processor.objectcreator.impl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.processor.ProtoBufProcessorHelper;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.GenerationFunctionPOJO;
import net.coru.kloadgen.randomtool.generator.ProtoBufGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;


public class ProtobufObjectCreatorFactory implements ObjectCreator {

  private Descriptors.Descriptor schema;

  private SchemaMetadata metadata;

  private RandomObject randomObject;

  private RandomMap randomMap;

  private ProtoBufGeneratorTool generatorTool;

  public ProtobufObjectCreatorFactory(Object schema, SchemaMetadata metadata) throws DescriptorValidationException, IOException {
    if(schema instanceof ParsedSchema) {
      this.schema = new ProtoBufProcessorHelper().buildDescriptor((ProtoFileElement) ((ParsedSchema)schema).rawSchema());
    }
    else if(schema instanceof ProtoFileElement) {
      this.schema = new ProtoBufProcessorHelper().buildDescriptor((ProtoFileElement)schema);
    }
    this.metadata = metadata;
    this.randomObject = new RandomObject();
    this.randomMap = new RandomMap();
    this.generatorTool = new ProtoBufGeneratorTool();
  }

  @Override
  public String generateString(final Integer valueLength) {
    return String.valueOf(randomObject.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()));
  }

  @Override
  public Object createMap(
      final String objectName, final ArrayDeque<?> fieldExpMappingsQueue, final String fieldName, final String completeFieldName, final Integer mapSize,
      final String completeTypeFilterChain, final String valueType,
      final Integer valueLength, final List<String> fieldValuesList, final int level, final BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createArray(
      final String objectName, final ArrayDeque<?> fieldExpMappingsQueue, final String fieldName, final String completeFieldName, final Integer arraySize,
      final String completeTypeFilterChain, final String valueType,
      final Integer valueLength, final List<String> fieldValuesList, final int level, final BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {
    return null;
  }

  @Override
  public Object createRepeatedObject(
      final String fieldName, final String completeFieldName, final String valueType, final Integer valueLength, final List<String> fieldValuesList) {
    return null;
  }

  @Override
  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    return null;
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
