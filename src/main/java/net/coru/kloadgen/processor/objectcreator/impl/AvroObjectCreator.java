package net.coru.kloadgen.processor.objectcreator.impl;

import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.BOOLEAN;
import static org.apache.avro.Schema.Type.BYTES;
import static org.apache.avro.Schema.Type.DOUBLE;
import static org.apache.avro.Schema.Type.FIXED;
import static org.apache.avro.Schema.Type.FLOAT;
import static org.apache.avro.Schema.Type.INT;
import static org.apache.avro.Schema.Type.LONG;
import static org.apache.avro.Schema.Type.MAP;
import static org.apache.avro.Schema.Type.NULL;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.STRING;
import static org.apache.avro.Schema.Type.UNION;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.common.ProcessorFieldTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.SchemaProcessorLib;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreator;
import net.coru.kloadgen.randomtool.generator.AvroGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections4.IteratorUtils;

public class AvroObjectCreator extends SchemaProcessorLib implements ProcessorObjectCreator {

  private Schema schema;

  private SchemaMetadata metadata;

  private AvroGeneratorTool avroGeneratorTool;

  private RandomObject randomObject;

  private RandomMap randomMap;

  private final Set<Type> typesSet = EnumSet.of(INT, DOUBLE, FLOAT, BOOLEAN, STRING, LONG, BYTES, FIXED);

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
  public EnrichedRecord createObjectChain(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    GenericRecord entity = new GenericData.Record(schema);
    ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = fieldExpMappingsQueue.clone();
    fieldExpMappingsQueueCopy.poll();
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;

    while (!fieldExpMappingsQueue.isEmpty()) {
      String cleanPath = cleanUpPath(fieldValueMapping, "");
      String fieldName = getCleanMethodName(fieldValueMapping, "");

      if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName))
          && (generatedProperties == elapsedProperties && generatedProperties > 0) && fieldValueMapping.getAncestorRequired()) {
        fieldValueMapping.setRequired(true);
        List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
        temporalFieldValueList.remove("null");
        fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
        fieldExpMappingsQueueCopy.poll();
      } else {
        generatedProperties = 0;
        elapsedProperties = 0;
        fieldExpMappingsQueueCopy.poll();
      }
      generatedProperties++;

      if (isOptionalField(schema.getField(fieldName)) && !fieldValueMapping.getRequired() && fieldValueMapping.getFieldValuesList().contains("null")) {
        elapsedProperties++;
        fieldExpMappingsQueue.remove();
        fieldValueMapping = fieldExpMappingsQueue.peek();
      } else {
        ProcessorFieldTypeEnum objectType = getFieldType(fieldValueMapping, cleanPath);
        switch (objectType) {
          case RECORD_MAP_MAP:
            createObjectMapMap(entity, fieldExpMappingsQueue, fieldName);
            break;
          case RECORD_ARRAY_ARRAY:
            createObjectArrayArray(entity, fieldExpMappingsQueue, fieldName);
            break;
          case RECORD_MAP_ARRAY:
            createObjectMapArray(entity, fieldExpMappingsQueue, fieldName);
            break;
          case RECORD_ARRAY_MAP:
            createObjectArrayMap(entity, fieldExpMappingsQueue, fieldName);
            break;
          case RECORD_MAP:
            createObjectMap(entity, fieldExpMappingsQueue, fieldName);
            break;
          case RECORD_ARRAY:
            createObjectArray(entity, fieldExpMappingsQueue, fieldName);
            break;
          case BASIC_MAP_MAP:
            createBasicMapMap(entity, fieldExpMappingsQueue, fieldName);
            break;
          case BASIC_ARRAY_ARRAY:
            createBasicArrayArray(entity, fieldExpMappingsQueue, fieldName);
            break;
          case BASIC_MAP_ARRAY:
            createBasicMapArray(entity, fieldExpMappingsQueue, fieldName);
            break;
          case BASIC_ARRAY_MAP:
            createBasicArrayMap(entity, fieldExpMappingsQueue, fieldName);
            break;
          case BASIC_MAP:
            createBasicMap(entity, fieldExpMappingsQueue, fieldName);
            break;
          case BASIC_ARRAY:
            createBasicArray(entity, fieldExpMappingsQueue, fieldName);
            break;
          case BASIC:
            createBasic(entity, fieldExpMappingsQueue, fieldName);
            break;
          case FINAL:
            createFinal(entity, fieldExpMappingsQueue, fieldValueMapping, fieldName);
            break;
          default:
            throw new KLoadGenException("Unimplemented data type processor " + objectType.name());
        }
      }
    }
    return new EnrichedRecord(metadata, entity);
  }

  public FieldValueMapping createObjectMapMap(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    throw new KLoadGenException("Unimplemented data type Record Map-Map");
  }

  public FieldValueMapping createObjectArrayArray(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    throw new KLoadGenException("Unimplemented data type Record Array-Array");
  }

  public FieldValueMapping createObjectMapArray(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    var recordArrayMap = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      recordArrayMap.add(createMap(extractType(this.schema.getField(fieldName), ARRAY).getElementType(), mapSize, temporalQueue, fieldName));
    }
    recordArrayMap.add(createMap(extractType(this.schema.getField(fieldName), ARRAY).getElementType(), arraySize, fieldExpMappingsQueue, fieldName));
    entity.put(fieldName, recordArrayMap);
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  public FieldValueMapping createObjectArrayMap(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

    Map<String, List> recordMapArray = new HashMap<>(mapSize);
    for (int i = 0; i < mapSize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      recordMapArray.put((String) randomObject.generateRandom("string", fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                         createArray(extractType(entity.getSchema().getField(fieldName), MAP).getValueType().getElementType(), arraySize, temporalQueue, fieldName));
    }
    recordMapArray.put((String) randomObject.generateRandom("string", fieldValueMapping.getValueLength(), Collections.emptyList(), Collections.emptyMap()),
                       createArray(extractType(entity.getSchema().getField(fieldName), MAP).getValueType().getElementType(), arraySize, fieldExpMappingsQueue, fieldName));
    entity.put(fieldName, recordMapArray);
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  public Object createObjectMap(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), getCleanMethodName(fieldValueMapping, fieldName));

    entity.put(fieldName, createMap(extractType(entity.getSchema().getField(fieldName), MAP).getValueType(),
                                          mapSize,
                                          fieldExpMappingsQueue,
                                          fieldName));
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  public Object createObjectArray(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), getCleanMethodName(fieldValueMapping, fieldName));

    entity.put(fieldName, createArray(extractType(entity.getSchema().getField(fieldName), ARRAY).getElementType(),
                                            arraySize,
                                            fieldExpMappingsQueue,
                                            fieldName));
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  public FieldValueMapping createBasicMapMap(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    return createBasicMap(entity, fieldExpMappingsQueue, fieldName);
  }

  public FieldValueMapping createBasicArrayArray(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    return createBasicArray(entity, fieldExpMappingsQueue, fieldName);
  }

  public Object createBasicMapArray(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.poll();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);

    var mapArray = randomMap.generateMap(fieldValueMapping.getFieldType(), mapSize, fieldValueMapping.getFieldValuesList(), fieldValueMapping.getValueLength(), arraySize,
                                         fieldValueMapping.getConstraints());

    entity.put(fieldName, mapArray);
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  public Object createBasicArrayMap(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    fieldExpMappingsQueue.remove();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    Integer mapSize = calculateMapSize(fieldValueMapping.getFieldName(), fieldName);
    var simpleTypeArrayMap = createSimpleTypeArrayMap(fieldName, fieldValueMapping.getFieldType(), arraySize, mapSize, fieldValueMapping.getValueLength(),
                                                      fieldValueMapping.getFieldValuesList());
    entity.put(fieldName, simpleTypeArrayMap);
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  public FieldValueMapping createBasicMap(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    fieldExpMappingsQueue.remove();
    // Add condition that checks (][)
    entity.put(fieldName, createSimpleTypeMap(fieldName, fieldValueMapping.getFieldType(),
                                              calculateMapSize(fieldValueMapping.getFieldName(), fieldName),
                                              fieldValueMapping.getValueLength(),
                                              fieldValueMapping.getFieldValuesList()));
    return fieldExpMappingsQueue.peek();
  }

  public FieldValueMapping createBasicArray(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();
    Integer arraySize = calculateSize(fieldValueMapping.getFieldName(), fieldName);
    entity.put(fieldName,
               createArray(fieldName, arraySize, fieldExpMappingsQueue));
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  public FieldValueMapping createBasic(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    entity.put(fieldName, createObject(entity.getSchema().getField(fieldName).schema(), fieldExpMappingsQueue, fieldName));
    return getSafeGetElement(fieldExpMappingsQueue);
  }

  public FieldValueMapping createFinal(GenericRecord entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, FieldValueMapping fieldValueMapping, final String fieldName) {
    entity.put(Objects.requireNonNull(fieldValueMapping).getFieldName(),
               avroGeneratorTool.generateObject(
                   entity.getSchema().getField(fieldName),
                   fieldValueMapping,
                   extractConstraints(schema.getField(fieldValueMapping.getFieldName()))
               )
    );
    fieldExpMappingsQueue.remove();
    return fieldExpMappingsQueue.peek();
  }

  public GenericRecord createObject(final Schema subSchema, final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String rootFieldName) {
    Schema innerSchema = subSchema;
    if (subSchema.getType().equals(MAP)) {
      innerSchema = subSchema.getValueType();
    } else if (subSchema.getType().equals(ARRAY)) {
      innerSchema = innerSchema.getElementType();
    }
    GenericRecord subEntity = createRecord(innerSchema);
    if (null == subEntity) {
      throw new KLoadGenException("Something Odd just happened");
    } else {
      subEntity.getSchema();
    }
    FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

    int generatedProperties = 0;
    int elapsedProperties = 0;

    while (!fieldExpMappingsQueue.isEmpty()
           && (Objects.requireNonNull(fieldValueMapping).getFieldName().matches(".*" + rootFieldName + "$")
               || fieldValueMapping.getFieldName().matches(rootFieldName + "\\..*")
               || fieldValueMapping.getFieldName().matches(".*" + rootFieldName + "\\[.*")
               || fieldValueMapping.getFieldName().matches(".*" + rootFieldName + "\\..*"))) {
      String cleanPath = cleanUpPath(fieldValueMapping, rootFieldName);
      String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, rootFieldName);

      generatedProperties++;

      if (((subSchema.getType().equals(RECORD) && isOptionalField(subSchema.getField(fieldNameSubEntity))) ||
           (subSchema.getType().equals(ARRAY) && isOptionalField(subSchema.getField(fieldNameSubEntity))) ||
           (subSchema.getType().equals(MAP) && isOptionalField(subSchema.getValueType().getField(fieldNameSubEntity))))
          && fieldValueMapping.getFieldValuesList().contains("null")) {

        elapsedProperties++;
        FieldValueMapping actualField = fieldExpMappingsQueue.peek();
        fieldExpMappingsQueue.remove();
        FieldValueMapping nextField = fieldExpMappingsQueue.peek();

        if (((fieldExpMappingsQueue.peek() != null && !Objects.requireNonNull(nextField).getFieldName().contains(rootFieldName))
             || fieldExpMappingsQueue.peek() == null)
            && actualField.getAncestorRequired()
            && (generatedProperties == elapsedProperties && generatedProperties > 0)) {

          fieldValueMapping = actualField;
          fieldValueMapping.setRequired(true);
          List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          if (fieldExpMappingsQueue.peek() == null) {
            fieldExpMappingsQueue.add(fieldValueMapping);
          }
        } else {
          fieldValueMapping = nextField;
        }

      } else {
        String mapFieldNameSubEntity;
        ProcessorFieldTypeEnum objectType = getFieldType(fieldValueMapping, fieldNameSubEntity);
        switch(objectType) {
          case RECORD_MAP_MAP:
            createObjectMapMap(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case RECORD_ARRAY_ARRAY:
            createObjectArrayArray(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case RECORD_MAP_ARRAY:
            createObjectMapArray(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case RECORD_ARRAY_MAP:
            createObjectArrayMap(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case RECORD_MAP:
            createObjectMap(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case RECORD_ARRAY:
            createObjectArray(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case BASIC_MAP_MAP:
            mapFieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, rootFieldName);
            createBasicMapMap(subEntity, fieldExpMappingsQueue, mapFieldNameSubEntity);
            break;
          case BASIC_ARRAY_ARRAY:
            createBasicArrayArray(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case BASIC_MAP_ARRAY:
            createBasicMapArray(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case BASIC_ARRAY_MAP:
            mapFieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, rootFieldName);
            createBasicArrayMap(subEntity, fieldExpMappingsQueue, mapFieldNameSubEntity);
            break;
          case BASIC_MAP:
            mapFieldNameSubEntity = getMapCleanMethodName(fieldValueMapping, rootFieldName);
            createBasicMap(subEntity, fieldExpMappingsQueue, mapFieldNameSubEntity);
            break;
          case BASIC_ARRAY:
            createBasicArray(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case BASIC:
            createBasic(subEntity, fieldExpMappingsQueue, fieldNameSubEntity);
            break;
          case FINAL:
            createFinal(subEntity, fieldExpMappingsQueue, fieldValueMapping, fieldNameSubEntity);
            break;
          default:
            throw new KLoadGenException("Unimplemented data type processor "+objectType.name());
        }
      }
    }
    return subEntity;
  }

  private List<GenericRecord> createArray(Schema subSchema, Integer arraySize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, String fieldName) {
    List<GenericRecord> objectArray = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectArray.add(createObject(subSchema, temporalQueue, fieldName));
    }
    objectArray.add(createObject(subSchema, fieldExpMappingsQueue, fieldName));
    return objectArray;
  }

  private Map<String, GenericRecord> createMap(Schema subSchema, Integer mapSize, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, String fieldName) {
    Map<String, GenericRecord> objectMap = new HashMap<>(mapSize);
    for (int i = 0; i < mapSize - 1; i++) {
      ArrayDeque<FieldValueMapping> temporalQueue = fieldExpMappingsQueue.clone();
      objectMap.put(generateMapKey(), createObject(subSchema, temporalQueue, fieldName));
    }
    objectMap.put(generateMapKey(), createObject(subSchema, fieldExpMappingsQueue, fieldName));
    return objectMap;
  }

  private Schema extractType(Field field, Type typeToMatch) {
    Schema realField = field.schema();
    if (UNION.equals(field.schema().getType())) {
      realField = IteratorUtils.find(field.schema().getTypes().iterator(), type -> typeToMatch.equals(type.getType()));
    }
    return realField;
  }

  private GenericRecord createRecord(Schema schema) {
    if (RECORD == schema.getType()) {
      return new GenericData.Record(schema);
    } else if (UNION == schema.getType()) {
      return createRecord(getRecordUnion(schema.getTypes()));
    } else if (ARRAY == schema.getType()) {
      return createRecord(schema.getElementType());
    } else if (MAP == schema.getType()) {
      return createRecord(schema.getElementType());
    } else {
      return null;
    }
  }

  private Schema getRecordUnion(List<Schema> types) {
    Schema isRecord = null;
    for (Schema innerSchema : types) {
      if (RECORD == innerSchema.getType() || ARRAY == innerSchema.getType() || MAP == innerSchema.getType() || typesSet.contains(innerSchema.getType())) {
        isRecord = innerSchema;
      }
    }
    return isRecord;
  }

  private Boolean isOptionalField(Field field) {
    if (UNION.equals(field.schema().getType())) {
      return IteratorUtils.matchesAny(field.schema().getTypes().iterator(), type -> type.getType() == NULL);
    }
    return false;
  }

  private Map<ConstraintTypeEnum, String> extractConstraints(Schema.Field field) {
    Map<ConstraintTypeEnum, String> constraints = new HashMap<>();

    if (Objects.nonNull(field.schema().getObjectProp("precision"))) {
      constraints.put(ConstraintTypeEnum.PRECISION, field.schema().getObjectProp("precision").toString());
    }

    if (Objects.nonNull(field.schema().getObjectProp("scale"))) {
      constraints.put(ConstraintTypeEnum.SCALE, field.schema().getObjectProp("scale").toString());
    }

    return constraints;
  }
}
