/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomIterator;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.random.RandomSequence;
import net.coru.kloadgen.randomtool.util.ValueUtils;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericFixed;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.RandomUtils;

public class AvroGeneratorTool {

  private final Map<String, Object> context = new HashMap<>();

  private final RandomObject randomObject = new RandomObject();

  private final RandomArray randomArray = new RandomArray();

  private final RandomMap randomMap = new RandomMap();

  public Object generateObject(final Schema schema, final FieldValueMapping fieldValueMapping, final Map<ConstraintTypeEnum, String> constraints) {
    return generateObject(schema, fieldValueMapping.getFieldName(), fieldValueMapping.getFieldType(), fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(),
                          constraints);
  }

  public Object generateObject(final String fieldName, final String fieldType, final Integer valueLength, final List<String> fieldValuesList,
      final Map<ConstraintTypeEnum, String> constraints) {
    final String fieldType = fieldValueMapping.getFieldType();
    final Integer valueLength = fieldValueMapping.getValueLength();
    final List<String> fieldValuesList = fieldValueMapping.getFieldValuesList();

    final List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    final boolean logicalType = Objects.nonNull(schema.getLogicalType());

    Object value;
    if (Type.ENUM == schema.getType() && !"seq".equalsIgnoreCase(fieldType) && !"it".equalsIgnoreCase(fieldType)) {
      value = getEnumOrGenerate(fieldValueMapping.getFieldName(), fieldType, schema, parameterList, schema.getType().getName());
    } else if (Type.UNION == schema.getType() && !"seq".equalsIgnoreCase(fieldType) && !"it".equalsIgnoreCase(fieldType)) {
      final Schema safeSchema = getRecordUnion(schema.getTypes());
      if (differentTypesNeedCast(fieldType, safeSchema.getType())) {

        value = randomObject.generateRandom(fieldType, valueLength, parameterList, constraints);
        value = ValueUtils.castValue(value, schema.getType().getName());
      } else if (Type.ENUM == safeSchema.getType()) {
        value = getEnumOrGenerate(fieldName, fieldType, safeSchema, parameterList, field.schema().getType().getName());
      } else {
        value = randomObject.generateRandom(fieldType, valueLength, parameterList, constraints);
        if ("null".equalsIgnoreCase(value.toString())) {
          value = null;
        }
      }
    } else if ("seq".equalsIgnoreCase(fieldType)) {
      final String type = Type.UNION.getName().equals(ValueUtils.getValidTypeFromSchema(field.schema())) ? getRecordUnion(field.schema().getTypes()).getName()
          : ValueUtils.getValidTypeFromSchema(schema);
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(type))) {
        return RandomSequence.generateSequenceForFieldValueList(fieldValueMapping.getFieldName(), type, fieldValuesList, context);
      } else {
        value = RandomSequence.generateSeq(field.name(), type, parameterList, context);
      }
    } else if ("it".equalsIgnoreCase(fieldType)) {
      String type = Type.UNION.getName().equals(ValueUtils.getValidTypeFromSchema(field.schema())) ? getRecordUnion(field.schema().getTypes()).getName()
          : ValueUtils.getValidTypeFromSchema(field.schema());
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomIterator.isTypeSupported(type))) {
        return RandomIterator.generateIteratorForFieldValueList(fieldValueMapping.getFieldName(), type, fieldValuesList, context);
      } else {
        value = RandomIterator.generateIt(fieldName, type, parameterList, context);
      }
    } else if (differentTypesNeedCast(fieldType, schemagetType())) {

      value = randomObject.generateRandom(fieldType, valueLength, parameterList, constraints);
      value = ValueUtils.castValue(value, field.schema().getType().getName());
    } else if (!logicalType && Type.FIXED == field.schema().getType()) {
      value = getFixedOrGenerate(field.schema());
    } else {
      value = randomObject.generateRandom(fieldType, valueLength, parameterList, constraints);
    }
    return value;
  }

  private boolean differentTypesNeedCast(final String fieldType, final Type fieldTypeSchema) {
    final boolean result;
    switch (fieldTypeSchema) {
      case RECORD:
      case ENUM:
      case ARRAY:
      case MAP:
      case UNION:
      case FIXED:
      case NULL:
        result = false;
        break;
      case STRING:
        result = needCastForString(fieldType);
        break;
      case INT:
        result = needCastForInt(fieldType);
        break;
      case LONG:
      case FLOAT:
      case DOUBLE:
      case BOOLEAN:
      case BYTES:
      default:
        result = !fieldTypeSchema.getName().equals(fieldType.split("_")[0]);
        break;
    }
    return result;
  }

  private GenericFixed getFixedOrGenerate(final Schema schema) {

    final byte[] bytes = new byte[schema.getFixedSize()];

    return new GenericData.Fixed(schema, bytes);
  }

  private boolean needCastForInt(final String fieldType) {
    final boolean result;
    switch (fieldType) {
      case "short":
      case "int":
        result = false;
        break;
      default:
        result = !Type.INT.getName().equals(fieldType.split("_")[0]);
        break;
    }
    return result;
  }

  private boolean needCastForString(final String fieldType) {
    final boolean result;
    switch (fieldType) {
      case "timestamp":
      case "uuid":
        result = false;
        break;
      default:
        result = !Type.STRING.getName().equals(fieldType);
        break;
    }
    return result;
  }

  private Object getEnumOrGenerate(final String fieldName, final String fieldType, final Schema schema, final List<String> parameterList, final String fieldValueMappingType) {
    final Object value;
    if ("ENUM".equalsIgnoreCase(fieldValueMappingType)) {
      if (parameterList.isEmpty()) {
        final List<String> enumValueList = schema.getEnumSymbols();
        value = new GenericData.EnumSymbol(schema, enumValueList.get(RandomUtils.nextInt(0, enumValueList.size())));
      } else {
        if ("Seq".equalsIgnoreCase(fieldType)) {
          value = new GenericData.EnumSymbol(schema, RandomSequence.generateSequenceForFieldValueList(fieldName, fieldValueMappingType, parameterList, context));
        } else if ("It".equalsIgnoreCase((fieldType))) {
          value = new GenericData.EnumSymbol(schema, RandomIterator.generateIteratorForFieldValueList(fieldName, fieldValueMappingType, parameterList, context));
        } else {
          value = new GenericData.EnumSymbol(schema, parameterList.get(RandomUtils.nextInt(0, parameterList.size())));
        }
      }
    } else {
      value = new GenericData.EnumSymbol(schema, fieldType);
    }
    return value;
  }

  private Schema getRecordUnion(final List<Schema> types) {
    return IterableUtils.find(types, schema -> !schema.getType().equals(Type.NULL));
  }

  public Object generateMap(
      String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList, Integer mapSize, Map<ConstraintTypeEnum, String> constraints) {
    final List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(
        fieldValue -> fieldValue.matches("\\$\\{\\w*}") ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) : fieldValue);

    final var value = new HashMap<>(mapSize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value.put(randomObject.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()),
                  RandomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
      } else {
        for (int i = mapSize; i > 0; i--) {
          value.put(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()),
                    RandomSequence.generateSeq(fieldName, fieldType, parameterList, CONTEXT));
        }
      }
    } else {
      return randomMap.generateMap(fieldType, mapSize, parameterList, valueLength, mapSize, constraints);
    }

    return value;
  }
}
