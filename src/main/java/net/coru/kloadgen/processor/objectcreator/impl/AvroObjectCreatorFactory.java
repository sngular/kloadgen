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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.GenerationFunctionPOJO;
import net.coru.kloadgen.randomtool.generator.AvroGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.random.RandomSequence;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections4.IteratorUtils;

public class AvroObjectCreatorFactory implements ObjectCreator {

  private final Set<Type> typesSet = EnumSet.of(INT, DOUBLE, FLOAT, BOOLEAN, STRING, LONG, BYTES, FIXED);

  private final Schema schema;

  private final SchemaMetadata metadata;

  private final AvroGeneratorTool avroGeneratorTool;

  private final RandomObject randomObject;

  private final Map<String, GenericRecord> entity = new HashMap<>();

  public AvroObjectCreatorFactory(Object schema, Object metadata) {
    if (schema instanceof ParsedSchema) {
      this.schema = (Schema) ((ParsedSchema) schema).rawSchema();
    } else {
      this.schema = (Schema) schema;
    }
    this.metadata = (SchemaMetadata) metadata;
    this.randomObject = new RandomObject();
    this.avroGeneratorTool = new AvroGeneratorTool();
  }

  @Override
  public String generateString(final Integer valueLength) {
    return String.valueOf(randomObject.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()));
  }

  @Override
  public Object createMap(
      final String objectName,
      final ArrayDeque<?> fieldExpMappingsQueue,
      final String fieldName,
      final String completeFieldName,
      final Integer mapSize,
      final String completeTypeFilterChain,
      final String valueType,
      final Integer valueLength,
      final List<String> fieldValuesList,
      final int level,
      final BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {

    final Map<String, Object> map = new HashMap<>();

    for (int i = 0; i < mapSize; i++) {
      GenerationFunctionPOJO pojo = new GenerationFunctionPOJO(objectName, fieldExpMappingsQueue, objectName, fieldName, completeFieldName,
                                                               completeTypeFilterChain, valueType, valueLength, fieldValuesList, level);
      if (i == mapSize - 1) {
        map.put(generateString(valueLength), generateFunction.apply(fieldExpMappingsQueue, pojo));
      } else {
        map.put(generateString(valueLength), generateFunction.apply(fieldExpMappingsQueue.clone(), pojo));
      }
    }
    entity.get(objectName).put(fieldName, map);
    return returnCompleteEntry ? entity.get(objectName) : map;
  }

  @Override
  public Object createArray(
      final String objectName,
      final ArrayDeque<?> fieldExpMappingsQueue,
      final String fieldName,
      final String completeFieldName,
      final Integer arraySize,
      final String completeTypeFilterChain,
      final String valueType,
      final Integer valueLength,
      final List<String> fieldValuesList,
      final int level,
      final BiFunction<ArrayDeque<?>, GenerationFunctionPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {

    final List<Object> list = new ArrayList<>();

    for (int i = 0; i < arraySize; i++) {
      GenerationFunctionPOJO pojo = new GenerationFunctionPOJO(objectName, fieldExpMappingsQueue, objectName, fieldName, completeFieldName,
                                                               completeTypeFilterChain, valueType, valueLength, fieldValuesList, level);
      if (i == arraySize - 1) {
        list.add(generateFunction.apply(fieldExpMappingsQueue, pojo));
      } else {
        list.add(generateFunction.apply(fieldExpMappingsQueue.clone(), pojo));
      }
    }
    entity.get(objectName).put(fieldName, list);
    return returnCompleteEntry ? entity.get(objectName) : list;
  }

  @Override
  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    GenericRecord entityObject = entity.get(targetObjectName);
    entityObject.put(fieldName, objectToAssign);
    return entityObject;
  }

  @Override
  public Object assignRecord(final String targetObjectName, final String fieldName, final String recordToAssign) {
    GenericRecord entityObject = entity.get(targetObjectName);
    entityObject.put(fieldName, entity.get(recordToAssign));
    return entity.get(targetObjectName);
  }

  public Object createRepeatedObject(
      final String fieldName, final String completeFieldName, final String fieldType, final Integer valueLength,
      List<String> fieldValuesList) {
    Schema fieldSchema = findSchema(fieldName, this.schema, new AtomicBoolean(false));
    return avroGeneratorTool.generateObject(
        Objects.requireNonNull(fieldSchema),
        completeFieldName, fieldType.substring(0, fieldType.indexOf("-") > 0 ? fieldType.indexOf("-") : fieldType.length()), valueLength, fieldValuesList,
        extractConstraints(fieldSchema)
    );
  }

  @Override
  public Object createRecord(final String objectName) {
    if ("root".equalsIgnoreCase(objectName)) {
      entity.put("root", new GenericData.Record(this.schema));
    } else {
      Schema innerSchema = findSchema(objectName, this.schema, new AtomicBoolean(false));
      if (innerSchema.getType().equals(Type.MAP)) {
        innerSchema = findRecursiveSchemaForRecord(innerSchema.getValueType());
      } else if (innerSchema.getType().equals(Type.ARRAY)) {
        innerSchema = findRecursiveSchemaForRecord(innerSchema.getElementType());
      } else if (innerSchema.getType().equals(Type.UNION)) {
        innerSchema = findRecursiveSchemaForRecord(getRecordUnion(innerSchema.getTypes()));
      }
      entity.put(objectName, new GenericData.Record(innerSchema));
    }
    return entity.get(objectName);
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

  @Override
  public Object generateRecord() {
    return new EnrichedRecord(this.metadata, this.entity.get("root"));
  }

  @Override
  public boolean isOptional(final String rootFieldName, final String fieldName) {
    var schema = findSchema(rootFieldName, this.schema, new AtomicBoolean(false));
    if (schema.getType().equals(Type.MAP)) {
      schema = findRecursiveSchemaForRecord(schema.getValueType());
    } else if (schema.getType().equals(Type.ARRAY)) {
      schema = findRecursiveSchemaForRecord(schema.getElementType());
    }

    return ((schema.getType().equals(RECORD) && isOptionalField(schema.getField(fieldName))) ||
            (schema.getType().equals(ARRAY) && isOptionalField(schema.getField(fieldName))) ||
            (schema.getType().equals(MAP) && isOptionalField(schema.getValueType().getField(fieldName))));

  }

  private Boolean isOptionalField(Field field) {
    if (UNION.equals(field.schema().getType())) {
      return IteratorUtils.matchesAny(field.schema().getTypes().iterator(), type -> type.getType() == NULL);
    }
    return false;
  }

  private Schema findSchema(final String objectName, final Schema fieldSchema, AtomicBoolean found) {
    Schema schema = fieldSchema;
    found.set(schema.getName().equalsIgnoreCase(objectName));
    if (!found.get()) {
      if (RECORD.equals(schema.getType())) {
        List<Field> fields = schema.getFields();
        var fieldListIt = fields.iterator();

        while (fieldListIt.hasNext() && !found.get()) {
          Schema.Field field = fieldListIt.next();
          found.set(field.name().equalsIgnoreCase(objectName));
          if (found.get()) {
            schema = field.schema();
          } else {
            schema = findSchema(objectName, field.schema(), found);
          }
        }
      } else if (ARRAY.equals(schema.getType())) {
        schema = findSchema(objectName, schema.getElementType(), found);
      } else if (MAP.equals(schema.getType())) {
        schema = findSchema(objectName, schema.getValueType(), found);
      } else if (UNION.equals(schema.getType())) {
        schema = findSchema(objectName, getRecordUnion(schema.getTypes()), found);
      }
    }
    return schema;
  }

  private Schema findRecursiveSchemaForRecord(final Schema fieldSchema) {
    Schema schema = fieldSchema;
    if (ARRAY.equals(schema.getType())) {
      schema = findRecursiveSchemaForRecord(schema.getElementType());
    } else if (MAP.equals(schema.getType())) {
      schema = findRecursiveSchemaForRecord(schema.getValueType());
    } else if (UNION.equals(schema.getType())) {
      schema = findRecursiveSchemaForRecord(getRecordUnion(schema.getTypes()));
    }
    return schema;
  }

  private Map<ConstraintTypeEnum, String> extractConstraints(Schema schema) {
    Map<ConstraintTypeEnum, String> constraints = new HashMap<>();

    if (Objects.nonNull(schema.getObjectProp("precision"))) {
      constraints.put(ConstraintTypeEnum.PRECISION, schema.getObjectProp("precision").toString());
    }

    if (Objects.nonNull(schema.getObjectProp("scale"))) {
      constraints.put(ConstraintTypeEnum.SCALE, schema.getObjectProp("scale").toString());
    }

    return constraints;
  }
}
