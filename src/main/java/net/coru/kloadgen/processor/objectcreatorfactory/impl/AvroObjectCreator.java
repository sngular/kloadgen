package net.coru.kloadgen.processor.objectcreatorfactory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.processor.model.SchemaProcessorPOJO;
import net.coru.kloadgen.processor.objectcreatorfactory.ObjectCreator;
import net.coru.kloadgen.processor.util.SchemaProcessorUtils;
import net.coru.kloadgen.randomtool.generator.AvroGeneratorTool;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections4.IteratorUtils;

public class AvroObjectCreator implements ObjectCreator {

  private static final AvroGeneratorTool AVRO_GENERATOR_TOOL = new AvroGeneratorTool();

  private static final Set<Type> TYPES_SET = EnumSet.of(Type.INT, Type.DOUBLE, Type.FLOAT, Type.BOOLEAN, Type.STRING, Type.LONG, Type.BYTES, Type.FIXED);

  private final Schema schema;

  private final SchemaMetadata metadata;

  private final Map<String, GenericRecord> entity = new HashMap<>();

  public AvroObjectCreator(final Object schema, final Object metadata) {
    if (schema instanceof ParsedSchema) {
      this.schema = (Schema) ((ParsedSchema) schema).rawSchema();
    } else if (schema instanceof Schema) {
      this.schema = (Schema) schema;
    } else {
      throw new KLoadGenException("Unsupported schema type");
    }
    this.metadata = (SchemaMetadata) metadata;
  }

  @Override
  public final Object createMap(
      final SchemaProcessorPOJO pojo, final Function<SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerMap) {

    Map<Object, Object> map = new HashMap<>();
    if (pojo.isLastFilterTypeOfLastElement()) {
      map = createFinalMap(pojo);
    } else {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        final String key = generateString(pojo.getValueLength());
        try {
          map.put(key, generateFunction.apply(i == pojo.getFieldSize() - 1 ? pojo : (SchemaProcessorPOJO) pojo.clone()));
        } catch (final CloneNotSupportedException e) {
          throw new KLoadGenException("Error cloning POJO");
        }
      }
    }
    entity.get(pojo.getRootFieldName()).put(pojo.getFieldNameSubEntity(), map);
    return isInnerMap ? map : entity.get(pojo.getRootFieldName());
  }

  private Map<Object, Object> createFinalMap(final SchemaProcessorPOJO pojo) {
    return (Map<Object, Object>) AVRO_GENERATOR_TOOL.generateMap(pojo.getFieldNameSubEntity(), SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()),
                                                                 pojo.getValueLength(), pojo.getFieldValuesList(), pojo.getFieldSize(), Collections.emptyMap());
  }

  private String generateString(final Integer valueLength) {
    return String.valueOf(AVRO_GENERATOR_TOOL.generateRawObject("string", valueLength, Collections.emptyList(), Collections.emptyMap()));
  }

  @Override
  public final Object createArray(
      final SchemaProcessorPOJO pojo, final Function<SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerArray) {

    List<Object> list = new ArrayList<>();
    if (pojo.isLastFilterTypeOfLastElement()) {
      list = createFinalArray(pojo);
    } else {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        try {
          list.add(generateFunction.apply(i == pojo.getFieldSize() - 1 ? pojo : (SchemaProcessorPOJO) pojo.clone()));
        } catch (final CloneNotSupportedException e) {
          throw new KLoadGenException("Error cloning POJO");
        }
      }
    }
    entity.get(pojo.getRootFieldName()).put(pojo.getFieldNameSubEntity(), list);
    return isInnerArray ? list : entity.get(pojo.getRootFieldName());
  }

  public final Object createValueObject(
      final SchemaProcessorPOJO pojo) {
    final Schema fieldSchema = findSchema(pojo.getFieldNameSubEntity(), this.schema, new AtomicBoolean(false));

    final Object valueObject = AVRO_GENERATOR_TOOL.generateObject(Objects.requireNonNull(fieldSchema), pojo.getCompleteFieldName(),
                                                                  SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()), pojo.getValueLength(),
                                                                  pojo.getFieldValuesList(), extractConstraints(fieldSchema));

    return assignObject(pojo.getRootFieldName(), pojo.getFieldNameSubEntity(), valueObject);
  }

  @Override
  public final void assignRecord(final SchemaProcessorPOJO pojo) {
    final GenericRecord entityObject = entity.get(pojo.getRootFieldName());
    entityObject.put(pojo.getFieldNameSubEntity(), entity.get(pojo.getFieldNameSubEntity()));
    entity.get(pojo.getRootFieldName());
  }

  @Override
  public final void createRecord(final String objectName, final String completeFieldName) {
    if ("root".equalsIgnoreCase(objectName)) {
      entity.put(objectName, new GenericData.Record(this.schema));
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
  public final Object generateRecord() {
    return EnrichedRecord.builder().schemaMetadata(metadata).genericRecord(this.entity.get("root")).build();
  }

  @Override
  public final Object generateSubEntityRecord(final Object objectRecord) {
    return objectRecord;
  }

  @Override
  public final boolean isOptionalFieldAccordingToSchema(final String completeFieldName, final String fieldName, final int level) {
    var subSchema = findSchema(fieldName, this.schema, new AtomicBoolean(false));
    if (subSchema.getType().equals(Type.MAP)) {
      subSchema = findRecursiveSchemaForRecord(subSchema.getValueType());
    } else if (subSchema.getType().equals(Type.ARRAY)) {
      subSchema = findRecursiveSchemaForRecord(subSchema.getElementType());
    }
    return isOptionalField(subSchema);
  }

  private List<Object> createFinalArray(final SchemaProcessorPOJO pojo) {
    return (ArrayList) AVRO_GENERATOR_TOOL.generateArray(pojo.getFieldNameSubEntity(), SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()), pojo.getValueLength(),
                                                         pojo.getFieldValuesList(), pojo.getFieldSize(), Collections.emptyMap());
  }

  public final Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    final GenericRecord entityObject = entity.get(targetObjectName);
    entityObject.put(fieldName, objectToAssign);
    return entityObject;
  }

  private Schema getRecordUnion(final List<Schema> types) {
    Schema isRecord = null;
    for (Schema innerSchema : types) {
      if (Type.RECORD == innerSchema.getType() || Type.ARRAY == innerSchema.getType() || Type.MAP == innerSchema.getType() || TYPES_SET.contains(innerSchema.getType())) {
        isRecord = innerSchema;
      }
    }
    return isRecord;
  }

  private Boolean isOptionalField(final Schema schema) {
    boolean isOptionalField = false;
    if (Type.UNION.equals(schema.getType())) {
      isOptionalField = IteratorUtils.matchesAny(schema.getTypes().iterator(), type -> type.getType() == Type.NULL);
    }
    return isOptionalField;
  }

  private Schema findSchema(final String objectName, final Schema fieldSchema, final AtomicBoolean found) {
    Schema subSchema = fieldSchema;
    found.set(subSchema.getName().equalsIgnoreCase(objectName));
    if (!found.get()) {
      if (Type.RECORD.equals(subSchema.getType())) {
        final List<Field> fields = subSchema.getFields();
        final var fieldListIt = fields.iterator();

        while (fieldListIt.hasNext() && !found.get()) {
          final Schema.Field field = fieldListIt.next();
          found.set(field.name().equalsIgnoreCase(objectName));
          if (found.get()) {
            subSchema = field.schema();
          } else {
            subSchema = findSchema(objectName, field.schema(), found);
          }
        }
      } else if (Type.ARRAY.equals(subSchema.getType())) {
        subSchema = findSchema(objectName, subSchema.getElementType(), found);
      } else if (Type.MAP.equals(subSchema.getType())) {
        subSchema = findSchema(objectName, subSchema.getValueType(), found);
      } else if (Type.UNION.equals(subSchema.getType())) {
        subSchema = findSchema(objectName, getRecordUnion(subSchema.getTypes()), found);
      }
    }
    return subSchema;
  }

  private Schema findRecursiveSchemaForRecord(final Schema fieldSchema) {
    Schema subSchema = fieldSchema;
    if (Type.ARRAY.equals(subSchema.getType())) {
      subSchema = findRecursiveSchemaForRecord(subSchema.getElementType());
    } else if (Type.MAP.equals(subSchema.getType())) {
      subSchema = findRecursiveSchemaForRecord(subSchema.getValueType());
    } else if (Type.UNION.equals(subSchema.getType())) {
      subSchema = findRecursiveSchemaForRecord(getRecordUnion(subSchema.getTypes()));
    }
    return subSchema;
  }

  private Map<ConstraintTypeEnum, String> extractConstraints(final Schema schema) {
    final Map<ConstraintTypeEnum, String> constraints = new HashMap<>();
    if (Objects.nonNull(schema.getObjectProp("precision"))) {
      constraints.put(ConstraintTypeEnum.PRECISION, schema.getObjectProp("precision").toString());
    }
    if (Objects.nonNull(schema.getObjectProp("scale"))) {
      constraints.put(ConstraintTypeEnum.SCALE, schema.getObjectProp("scale").toString());
    }
    return constraints;
  }
}
