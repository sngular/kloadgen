package net.coru.kloadgen.processor.objectcreator.impl;

import static org.apache.avro.Schema.Type.ARRAY;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.common.ProcessorFieldTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreator;
import net.coru.kloadgen.randomtool.generator.AvroGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

public class AvroObjectCreator implements ProcessorObjectCreator {

  private Schema schema;

  private SchemaMetadata metadata;

  private AvroGeneratorTool avroGeneratorTool;

  private RandomObject randomObject;

  private RandomMap randomMap;

  private Map<String, GenericRecord> entity = new HashMap<>();

  public AvroObjectCreator(Object schema, SchemaMetadata metadata) {
    if(schema instanceof ParsedSchema) {
      this.schema = (Schema) ((ParsedSchema)schema).rawSchema();
    }
    else if(schema instanceof Schema) {
      this.schema = (Schema) schema;
    }
    this.metadata = metadata;
    this.randomObject = new RandomObject();
    this.randomMap = new RandomMap();
    this.avroGeneratorTool = new AvroGeneratorTool();
  }

  @Override
  public Object createObject(final ProcessorFieldTypeEnum objectType, Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    switch(objectType) {
      case OBJECT_MAP_MAP:
        createObjectMapMap(entity, fieldExpMappingsQueue, fieldName);
        return null;
      case OBJECT_ARRAY_ARRAY:
        createObjectArrayArray(entity, fieldExpMappingsQueue, fieldName);
        return null;
      case OBJECT_MAP_ARRAY:
        createObjectMapArray(entity, fieldExpMappingsQueue, fieldName);
        return null;
      case OBJECT_ARRAY_MAP:
        createObjectArrayMap(entity, fieldExpMappingsQueue, fieldName);
        return null;
      case OBJECT_MAP:
        createObjectMap(entity, fieldExpMappingsQueue, fieldName);
        return null;
      case OBJECT_ARRAY:
        createObjectArray(entity, fieldExpMappingsQueue, fieldName);
        return null;
      case BASIC_MAP_MAP:

        return null;
      case BASIC_ARRAY_ARRAY:

        return null;
      case BASIC_MAP_ARRAY:

        return null;
      case BASIC_ARRAY_MAP:

        return null;
      case BASIC_MAP:

        return null;
      case BASIC_ARRAY:

        return null;
      case BASIC:

        return null;
      case FINAL:

        return null;
      default:

        return null;
    }
  }

  public Object createObjectMapMap(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createObjectArrayArray(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createObjectMapArray(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    var recordArrayMap = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      recordArrayMap.add(createObjectMap(extractType(entity.getSchema().getField(fieldName), ARRAY).getElementType(), fieldName, mapSize, temporalQueue));
    }
    recordArrayMap.add(createObjectMap(extractType(entity.getSchema().getField(fieldName), ARRAY).getElementType(), fieldName, arraySize, fieldExpMappingsQueue));
    entity.put(fieldName, recordArrayMap);
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  public Object createObjectArrayMap(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createObjectMap(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createObjectArray(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createBasicMapMap(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createBasicArrayArray(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createBasicMapArray(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createBasicArrayMap(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createBasicMap(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createBasicArray(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createBasic(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }

  public Object createFinal(Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {

  }
}
