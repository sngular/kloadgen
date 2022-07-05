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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;
import net.coru.kloadgen.randomtool.generator.AvroGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections4.IteratorUtils;

public class AvroObjectCreatorFactory implements ObjectCreator {

  private static final AvroGeneratorTool AVRO_GENERATOR_TOOL = new AvroGeneratorTool();

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  private static final Set<Type> TYPES_SET = EnumSet.of(INT, DOUBLE, FLOAT, BOOLEAN, STRING, LONG, BYTES, FIXED);

  private final Schema schema;

  private final SchemaMetadata metadata;

  private final Map<String, GenericRecord> entity = new HashMap<>();

  public AvroObjectCreatorFactory(Object schema, Object metadata) {
    if (schema instanceof ParsedSchema) {
      this.schema = (Schema) ((ParsedSchema) schema).rawSchema();
    } else {
      this.schema = (Schema) schema;
    }
    this.metadata = (SchemaMetadata) metadata;
  }

  @Override
  public Object createMap(
      final SchemaProcessorPOJO pojo,
      final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {

    Map<Object, Object> map = new HashMap<>();
    if (pojo.isLastFilterTypeOfLastElement()) {
      map = createFinalMap(pojo);
    } else {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        String key = generateString(pojo.getValueLength());
        if (i == pojo.getFieldSize() - 1) {
          map.put(key, generateFunction.apply(pojo.getFieldExpMappingsQueue(), pojo));
        } else {
          map.put(key, generateFunction.apply(pojo.getFieldExpMappingsQueue().clone(), pojo));
        }
      }
    }
    entity.get(pojo.getRootFieldName()).put(pojo.getFieldNameSubEntity(), map);
    return returnCompleteEntry ? entity.get(pojo.getRootFieldName()) : map;
  }

  private Map<Object, Object> createFinalMap(SchemaProcessorPOJO pojo) {
    return (Map<Object, Object>) AVRO_GENERATOR_TOOL.generateMap(pojo.getFieldNameSubEntity(), getSimpleValueType(pojo.getValueType()), pojo.getValueLength(),
                                                                 pojo.getFieldValuesList(),
                                                                 pojo.getFieldSize(),
                                                                 Collections.emptyMap());
  }

  private String generateString(final Integer valueLength) {
    return String.valueOf(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()));
  }

  private String getSimpleValueType(String completeValueType) {
    return completeValueType.substring(0, completeValueType.indexOf("-") > 0 ? completeValueType.indexOf("-") : completeValueType.length());
  }

  @Override
  public Object createArray(
      final SchemaProcessorPOJO pojo,
      final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction,
      final boolean returnCompleteEntry) {

    List<Object> list = new ArrayList<>();
    if (pojo.isLastFilterTypeOfLastElement()) {
      list = createFinalArray(pojo);
    } else {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        if (i == pojo.getFieldSize() - 1) {
          list.add(generateFunction.apply(pojo.getFieldExpMappingsQueue(), pojo));
        } else {
          list.add(generateFunction.apply(pojo.getFieldExpMappingsQueue().clone(), pojo));
        }
      }
    }
    entity.get(pojo.getRootFieldName()).put(pojo.getFieldNameSubEntity(), list);
    return returnCompleteEntry ? entity.get(pojo.getRootFieldName()) : list;
  }

  private List<Object> createFinalArray(SchemaProcessorPOJO pojo) {
    return (ArrayList) AVRO_GENERATOR_TOOL.generateArray(pojo.getFieldNameSubEntity(), getSimpleValueType(pojo.getValueType()), pojo.getValueLength(), pojo.getFieldValuesList(),
                                                         pojo.getFieldSize(),
                                                         Collections.emptyMap());
  }

  public Object createValueObject(
      final SchemaProcessorPOJO pojo) {
    Schema fieldSchema = findSchema(pojo.getFieldNameSubEntity(), this.schema, new AtomicBoolean(false));


    Object valueObject =  AVRO_GENERATOR_TOOL.generateObject(
        Objects.requireNonNull(fieldSchema),
        pojo.getCompleteFieldName(), getSimpleValueType(pojo.getValueType()), pojo.getValueLength(), pojo.getFieldValuesList(),
        extractConstraints(fieldSchema)
    );

    return assignObject(pojo.getRootFieldName(),pojo.getFieldNameSubEntity(), valueObject);
  }

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

  @Override
  public void createRecord(final String objectName) {
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
  }

  @Override
  public Object generateRecord() {
    return EnrichedRecord.builder().schemaMetadata(metadata).genericRecord(this.entity.get("root")).build();
  }

  @Override
  public boolean isOptional(final String rootFieldName, final String fieldName) {
    var schema = findSchema(fieldName, this.schema, new AtomicBoolean(false));
    if (schema.getType().equals(Type.MAP)) {
      schema = findRecursiveSchemaForRecord(schema.getValueType());
    } else if (schema.getType().equals(Type.ARRAY)) {
      schema = findRecursiveSchemaForRecord(schema.getElementType());
    }
    return isOptionalField(schema);
  }

  private Schema getRecordUnion(List<Schema> types) {
    Schema isRecord = null;
    for (Schema innerSchema : types) {
      if (RECORD == innerSchema.getType() || ARRAY == innerSchema.getType() || MAP == innerSchema.getType() || TYPES_SET.contains(innerSchema.getType())) {
        isRecord = innerSchema;
      }
    }
    return isRecord;
  }

  private Boolean isOptionalField(Schema schema) {
    if (UNION.equals(schema.getType())) {
      return IteratorUtils.matchesAny(schema.getTypes().iterator(), type -> type.getType() == NULL);
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
