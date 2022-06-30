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
import java.util.function.BiFunction;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ObjectFactory;
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

public class AvroFactory implements ObjectFactory {

  private final Set<Type> typesSet = EnumSet.of(INT, DOUBLE, FLOAT, BOOLEAN, STRING, LONG, BYTES, FIXED);

  private final Schema schema;

  private final SchemaMetadata metadata;

  private final AvroGeneratorTool avroGeneratorTool;

  private final RandomObject randomObject;

  private final RandomMap randomMap;

  private final RandomArray randomArray;

  private static final RandomSequence randomSequence = new RandomSequence();

  private final Map<String, GenericRecord> entity = new HashMap<>();

  public AvroFactory(Object schema, Object metadata) {
    if (schema instanceof ParsedSchema) {
      this.schema = (Schema) ((ParsedSchema) schema).rawSchema();
    } else {
      this.schema = (Schema) schema;
    }
    this.metadata = (SchemaMetadata) metadata;
    this.randomObject = new RandomObject();
    this.randomMap = new RandomMap();
    this.randomArray = new RandomArray();
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
      if(i == mapSize-1) {
        map.put(generateString(valueLength), generateFunction.apply(fieldExpMappingsQueue, pojo));
      }
      else {
        map.put(generateString(valueLength), generateFunction.apply(fieldExpMappingsQueue.clone(), pojo));
      }
    }
    entity.get(objectName).put(fieldName, map);
    return returnCompleteEntry?entity.get(objectName):map;
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
      if(i == arraySize-1) {
        list.add(generateFunction.apply(fieldExpMappingsQueue, pojo));
      }
      else {
        list.add(generateFunction.apply(fieldExpMappingsQueue.clone(), pojo));
      }
    }
    entity.get(objectName).put(fieldName, list);
    return returnCompleteEntry?entity.get(objectName):list;
  }

  @Override
  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    Map<String, GenericRecord> clonedMap = (Map<String, GenericRecord>) ((HashMap) entity).clone();
    GenericRecord entityObject = clonedMap.get(targetObjectName);
    entityObject.put(fieldName, objectToAssign);
    return entityObject;
  }

  public Object createFinalObject(final String targetObjectName, final String fieldName, final String fieldType, final Integer valueLength, List<String> fieldValuesList) {
    entity.get(targetObjectName).put(fieldName, avroGeneratorTool.generateObject(
        Objects.requireNonNull(findInFields(fieldName, this.schema.getFields())),
        fieldName, fieldType.substring(0, fieldType.indexOf("-") > 0 ? fieldType.indexOf("-") : fieldType.length()), valueLength, fieldValuesList,
        extractConstraints(findInFields(fieldName, this.schema.getFields()))
    ));
    return entity.get(targetObjectName);
  }

  public Object createRepeatedObject(final String targetObjectName, final String fieldName, final String fieldType, final Integer valueLength, List<String> fieldValuesList) {
    return avroGeneratorTool.generateObject(
        Objects.requireNonNull(findInFields(fieldName, this.schema.getFields())),
        fieldName, fieldType.substring(0, fieldType.indexOf("-") > 0 ? fieldType.indexOf("-") : fieldType.length()), valueLength, fieldValuesList,
        extractConstraints(findInFields(fieldName, this.schema.getFields()))
    );
  }

  /*public Object createBasicMap(final String fieldName, final String fieldType, final Integer mapSize, final Integer fieldValueLength, final List<String> fieldValuesList) {
    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}") ?
                                     JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                     fieldValue
    );

    var value = new HashMap<>(mapSize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomSequence.isTypeSupported(fieldType))) {
        value.put(generateMapKey(), randomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, context));
      } else {
        for (int i = mapSize; i > 0; i--) {
          value.put(generateMapKey(), randomSequence.generateSeq(fieldName, fieldType, parameterList, context));
        }
      }
    } else {
      return randomMap.generateMap(fieldType, mapSize, parameterList, fieldValueLength, 0, Collections.emptyMap());
    }

    return value;
  }*/

  @Override
  public Object createRecord(final String objectName) {
    if ("root".equalsIgnoreCase(objectName)) {
      entity.put("root", new GenericData.Record(this.schema));
    } else {
      Schema innerSchema = findSchema(objectName, this.schema);
      if (innerSchema.getType().equals(Type.MAP)) {
        innerSchema = findRecursiveSchemaForRecord(innerSchema.getValueType());
      } else if (innerSchema.getType().equals(Type.ARRAY)) {
        innerSchema = findRecursiveSchemaForRecord(innerSchema.getElementType());
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
    var schema = findSchema(rootFieldName, this.schema);
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

  private Field findInFields(final String objectName, final List<Field> fields) {
    var fieldLlistIt = fields.iterator();
    boolean found = false;
    Schema.Field field = null;
    while (fieldLlistIt.hasNext() && !found) {
      field = fieldLlistIt.next();
      found = field.name().equalsIgnoreCase(objectName);
      if (!found) {
        if (RECORD.equals(field.schema().getType())) {
          field = findInFields(objectName, field.schema().getFields());
        } else if (ARRAY.equals(field.schema().getType())) {
          field = findInFields(objectName, field.schema().getElementType().getFields());
        } else if (MAP.equals(field.schema().getType())) {
          field = findInFields(objectName, field.schema().getValueType().getElementType().getFields());
        }
        found = field.name().equalsIgnoreCase(objectName);
      }
    }
    return field;
  }

  private Schema findSchema(final String objectName, final Schema fieldSchema) {
    Schema schema = fieldSchema;
    boolean found = schema.getName().equalsIgnoreCase(objectName);
    if (!found) {
      if (RECORD.equals(schema.getType())) {
        List<Field> fields = schema.getFields();
        var fieldListIt = fields.iterator();
        while (fieldListIt.hasNext() && !found) {
          Schema.Field field = fieldListIt.next();
          found = field.name().equalsIgnoreCase(objectName);
          if (found) {
            schema = field.schema();
          } else {
            schema = findSchema(objectName, field.schema());
          }
          found = field.name().equalsIgnoreCase(objectName);
        }
      } else if (ARRAY.equals(schema.getType())) {
        schema = findSchema(objectName, schema.getElementType());
      } else if (MAP.equals(schema.getType())) {
        schema = findSchema(objectName, schema.getValueType());
      } else if (UNION.equals(schema.getType())) {
        schema = findSchema(objectName, getRecordUnion(schema.getTypes()));
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
