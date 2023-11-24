package com.sngular.kloadgen.processor.objectcreatorfactory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.processor.model.SchemaProcessorPOJO;
import com.sngular.kloadgen.processor.objectcreatorfactory.ObjectCreatorFactory;
import com.sngular.kloadgen.processor.util.SchemaProcessorUtils;
import com.sngular.kloadgen.randomtool.generator.AvroGeneratorTool;
import com.sngular.kloadgen.schemaregistry.adapter.impl.AbstractParsedSchemaAdapter;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ApicurioAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections4.IteratorUtils;

public final class AvroObjectCreatorFactory implements ObjectCreatorFactory {

  private static final AvroGeneratorTool AVRO_GENERATOR_TOOL = new AvroGeneratorTool();

  private static final Set<Type> TYPES_SET = EnumSet.of(Type.INT, Type.DOUBLE, Type.FLOAT, Type.BOOLEAN, Type.STRING, Type.LONG, Type.BYTES, Type.FIXED);

  private final Schema schema;

  private final SchemaMetadataAdapter schemaMetadataAdapter;

  private final Map<String, GenericRecord> entity = new HashMap<>();

  public AvroObjectCreatorFactory(final Object schema, final BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata) {
    if (schema instanceof ParsedSchema) {
      this.schema = (Schema) ((ParsedSchema) schema).rawSchema();
    } else if (schema instanceof Schema) {
      this.schema = (Schema) schema;
    } else if (schema instanceof BaseParsedSchema) {
      final BaseParsedSchema schemaParse = (BaseParsedSchema) schema;
      final AbstractParsedSchemaAdapter adapterParse = schemaParse.getParsedSchemaAdapter();
      if (adapterParse instanceof ApicurioAbstractParsedSchemaMetadata) {
        this.schema = (Schema) ((ApicurioAbstractParsedSchemaMetadata) adapterParse).getSchema();
      } else {
        this.schema = adapterParse.getRawSchema();
      }
    } else {
      throw new KLoadGenException("Unsupported schema type");
    }

    this.schemaMetadataAdapter = metadata.getSchemaMetadataAdapter();
  }

  @Override
  public Object createMap(final SchemaProcessorPOJO pojo, final Function<SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerMap) {

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
  public Object createArray(final SchemaProcessorPOJO pojo, final Function<SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerArray) {

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

  @Override
  public Object createValueObject(final SchemaProcessorPOJO pojo) {
    final Schema fieldSchema = findSchema(pojo.getCompleteFieldName(), this.schema, new AtomicBoolean(false));
    final String valueType;
    final boolean logicalType = Objects.nonNull(fieldSchema.getLogicalType());
    if (!logicalType) {
      valueType = SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType());
    } else {
      valueType = pojo.getValueType();
    }
    final Object valueObject = AVRO_GENERATOR_TOOL.generateObject(Objects.requireNonNull(fieldSchema), pojo.getCompleteFieldName(),
                                                                  valueType, pojo.getValueLength(),
                                                                  pojo.getFieldValuesList(), extractConstraints(fieldSchema));

    return assignObject(pojo.getRootFieldName(), pojo.getFieldNameSubEntity(), valueObject);
  }

  @Override
  public void assignRecord(final SchemaProcessorPOJO pojo) {
    final GenericRecord entityObject = entity.get(pojo.getRootFieldName());
    entityObject.put(pojo.getFieldNameSubEntity(), entity.get(pojo.getFieldNameSubEntity()));
    entity.get(pojo.getRootFieldName());
  }

  @Override
  public void createRecord(final String objectName, final String completeFieldName) {
    if ("root".equalsIgnoreCase(objectName)) {
      var aux = this.schema;
      if (Schema.Type.UNION.equals(this.schema.getType())) {
        aux = this.schema.getTypes().get(this.schema.getTypes().size() - 1);
      }
      entity.put(objectName, new GenericData.Record(aux));
    } else {
      Schema innerSchema = findSchema(completeFieldName, this.schema, new AtomicBoolean(false));
      if (innerSchema.getType().equals(Type.MAP)) {
        innerSchema = findRecursiveSchemaForRecord(innerSchema.getValueType());
      } else if (innerSchema.getType().equals(Type.ARRAY)) {
        innerSchema = findRecursiveSchemaForRecord(innerSchema.getElementType());
      } else if (innerSchema.getType().equals(Type.UNION)) {
        innerSchema = getRecordUnion(innerSchema.getTypes()).map(this::findRecursiveSchemaForRecord).orElse(innerSchema);
      }
      entity.put(objectName, new GenericData.Record(innerSchema));
    }
  }

  @Override
  public Object generateRecord() {
    return EnrichedRecord.builder().schemaMetadata(schemaMetadataAdapter).genericRecord(this.entity.get("root")).build();
  }

  @Override
  public Object generateSubEntityRecord(final Object objectRecord) {
    return objectRecord;
  }

  @Override
  public boolean isOptionalFieldAccordingToSchema(final String completeFieldName, final String fieldName, final int level) {
    var subSchema = findSchema(completeFieldName, this.schema, new AtomicBoolean(false));

    if (subSchema.getType().equals(Type.MAP)) {
      subSchema = findRecursiveSchemaForRecord(subSchema.getValueType());
    } else if (subSchema.getType().equals(Type.ARRAY)) {
      subSchema = findRecursiveSchemaForRecord(subSchema.getElementType());
    }
    return isOptionalField(subSchema);
  }

  public final Object getRootNode(final String rootNode) {
    return entity.get(rootNode);
  }

  private List<Object> createFinalArray(final SchemaProcessorPOJO pojo) {
    return (ArrayList) AVRO_GENERATOR_TOOL.generateArray(pojo.getFieldNameSubEntity(), SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()), pojo.getValueLength(),
                                                         pojo.getFieldValuesList(), pojo.getFieldSize(), Collections.emptyMap());
  }

  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    final GenericRecord entityObject = entity.get(targetObjectName);
    entityObject.put(fieldName, objectToAssign);
    return entityObject;
  }

  private Optional<Schema> getRecordUnion(final List<Schema> types) {
    Schema isRecord = null;
    for (final Schema innerSchema : types) {
      if (Type.RECORD == innerSchema.getType() || Type.ARRAY == innerSchema.getType() || Type.MAP == innerSchema.getType() || TYPES_SET.contains(innerSchema.getType())) {
        isRecord = innerSchema;
      }
    }
    return Optional.ofNullable(isRecord);
  }

  private Boolean isOptionalField(final Schema schema) {
    boolean isOptionalField = false;
    if (Type.UNION.equals(schema.getType())) {
      isOptionalField = IteratorUtils.matchesAny(schema.getTypes().iterator(), type -> type.getType() == Type.NULL);
    }
    return isOptionalField;
  }

  private Schema findSchema(final String fieldNamePath, final Schema fieldSchema, final AtomicBoolean found) {
    Schema subSchema = fieldSchema;
    final String[] arrNormalizedFieldNamePath = SchemaProcessorUtils.splitAndNormalizeFullFieldName(fieldNamePath);
    final boolean isLastSubField = arrNormalizedFieldNamePath.length == 1;
    final String normalizedFieldNamePath = arrNormalizedFieldNamePath[0];
    found.set(subSchema.getName().equalsIgnoreCase(normalizedFieldNamePath));
    if (!found.get()) {
      if (Type.RECORD.equals(subSchema.getType())) {
        final Schema.Field field = subSchema.getField(normalizedFieldNamePath);
        found.set(field.name().equalsIgnoreCase(normalizedFieldNamePath) && isLastSubField);
        if (found.get()) {
          subSchema = field.schema();
        } else {
          subSchema = findSchema(SchemaProcessorUtils.removeFieldPathFirstElement(fieldNamePath), subSchema.getField(normalizedFieldNamePath).schema(), found);
        }
      } else if (Type.ARRAY.equals(subSchema.getType())) {
        subSchema = findSchema(fieldNamePath, subSchema.getElementType(), found);
      } else if (Type.MAP.equals(subSchema.getType())) {
        subSchema = findSchema(fieldNamePath, subSchema.getValueType(), found);
      } else if (Type.UNION.equals(subSchema.getType())) {
        subSchema = getRecordUnion(subSchema.getTypes()).map(type -> findSchema(fieldNamePath, type, found)).orElse(subSchema);
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
      subSchema = getRecordUnion(subSchema.getTypes()).map(this::findRecursiveSchemaForRecord).orElse(subSchema);
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
