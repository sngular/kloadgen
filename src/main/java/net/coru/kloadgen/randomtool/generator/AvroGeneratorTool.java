/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomIterator;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.random.RandomSequence;
import net.coru.kloadgen.randomtool.util.ValueUtils;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericFixed;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.jmeter.threads.JMeterContextService;

public class AvroGeneratorTool {

  private final static Map<String, Object> CONTEXT = new HashMap<>();

  private final static RandomObject RANDOM_OBJECT = new RandomObject();

  private final static RandomArray RANDOM_ARRAY = new RandomArray();

  private final static RandomMap RANDOM_MAP = new RandomMap();

  public Object generateObject(final Schema schema, final FieldValueMapping fieldValueMapping, final Map<ConstraintTypeEnum, String> constraints) {
    return generateObject(schema, fieldValueMapping.getFieldName(), fieldValueMapping.getFieldType(), fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(),
                          constraints);
  }

  public Object generateObject(Schema schema, String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList, Map<ConstraintTypeEnum, String> constraints) {

    final List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    final boolean logicalType = Objects.nonNull(schema.getLogicalType());

    Object value;
    if (Type.ENUM == schema.getType() && !"seq".equalsIgnoreCase(fieldType) && !"it".equalsIgnoreCase(fieldType)) {
      value = getEnumOrGenerate(fieldName, fieldType, schema, parameterList, schema.getType().getName());
    } else if (Type.UNION == schema.getType() && !"seq".equalsIgnoreCase(fieldType) && !"it".equalsIgnoreCase(fieldType)) {
      final Schema safeSchema = getRecordUnion(schema.getTypes());
      if (differentTypesNeedCast(fieldType, safeSchema.getType())) {

        value = RANDOM_OBJECT.generateRandom(fieldType, valueLength, parameterList, constraints);
        value = ValueUtils.castValue(value, schema.getType().getName());
      } else if (Type.ENUM == safeSchema.getType()) {
        value = getEnumOrGenerate(fieldName, fieldType, safeSchema, parameterList, schema.getType().getName());
      } else {
        value = RANDOM_OBJECT.generateRandom(fieldType, valueLength, parameterList, constraints);
        if ("null".equalsIgnoreCase(value.toString())) {
          value = null;
        }
      }
    } else if ("seq".equalsIgnoreCase(fieldType)) {
      final String type = Type.UNION.getName().equals(ValueUtils.getValidTypeFromSchema(schema)) ? getRecordUnion(schema.getTypes()).getName()
                              : ValueUtils.getValidTypeFromSchema(schema);
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(type))) {
        return RandomSequence.generateSequenceForFieldValueList(fieldName, type, fieldValuesList, CONTEXT);
      } else {
        value = RandomSequence.generateSeq(fieldName, type, parameterList, CONTEXT);
      }
    } else if ("it".equalsIgnoreCase(fieldType)) {
      String type = Type.UNION.getName().equals(ValueUtils.getValidTypeFromSchema(schema)) ? getRecordUnion(schema.getTypes()).getName()
                        : ValueUtils.getValidTypeFromSchema(schema);
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomIterator.isTypeSupported(type))) {
        return RandomIterator.generateIteratorForFieldValueList(fieldName, type, fieldValuesList, CONTEXT);
      } else {
        value = RandomIterator.generateIt(fieldName, type, parameterList, CONTEXT);
      }
    } else if (differentTypesNeedCast(fieldType, schema.getType())) {

      value = RANDOM_OBJECT.generateRandom(fieldType, valueLength, parameterList, constraints);
      value = ValueUtils.castValue(value, schema.getType().getName());
    } else if (!logicalType && Type.FIXED == schema.getType()) {
      value = getFixedOrGenerate(schema);
    } else {
      value = RANDOM_OBJECT.generateRandom(fieldType, valueLength, parameterList, constraints);
    }
    return value;
  }

  private Object getEnumOrGenerate(final String fieldName, final String fieldType, final Schema schema, final List<String> parameterList, final String fieldValueMappingType) {
    final Object value;
    if ("ENUM".equalsIgnoreCase(fieldValueMappingType)) {
      if (parameterList.isEmpty()) {
        final List<String> enumValueList = schema.getEnumSymbols();
        value = new GenericData.EnumSymbol(schema, enumValueList.get(RandomUtils.nextInt(0, enumValueList.size())));
      } else {
        if ("Seq".equalsIgnoreCase(fieldType)) {
          value = new GenericData.EnumSymbol(schema, RandomSequence.generateSequenceForFieldValueList(fieldName, fieldValueMappingType, parameterList, CONTEXT));
        } else if ("It".equalsIgnoreCase((fieldType))) {
          value = new GenericData.EnumSymbol(schema, RandomIterator.generateIteratorForFieldValueList(fieldName, fieldValueMappingType, parameterList, CONTEXT));
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

  public Object generateRawObject(
      String fieldType, Integer valueLength, List<String> fieldValuesList, Map<ConstraintTypeEnum, String> constraints) {
    return RANDOM_OBJECT.generateRandom(fieldType, valueLength, fieldValuesList, constraints);
  }

  public Object generateArray(
      final String fieldName, final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Integer arraySize,
      final Map<ConstraintTypeEnum, String> constraints) {
    final List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(
        fieldValue -> fieldValue.matches("\\$\\{\\w*}") ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) : fieldValue);

    final List value = new ArrayList<>(valueLength);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value.add(RandomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
      } else if ("it".equals(fieldType)) {
        if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomIterator.isTypeSupported(fieldType))) {
          value.add(RandomIterator.generateIteratorForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
        } else {
          for (int i = arraySize; i > 0; i--) {
            value.add(RandomIterator.generateIt(fieldName, fieldType, parameterList, CONTEXT));
          }
        }
      } else {
        for (int i = valueLength; i > 0; i--) {
          value.add(RandomSequence.generateSeq(fieldName, fieldType, parameterList, CONTEXT));
        }
      }
    } else {
      value.addAll((ArrayList) RANDOM_ARRAY.generateArray(fieldType, valueLength, parameterList, arraySize, constraints));
    }

    return value;
  }

  public Object generateMap(
      final String fieldName, final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Integer mapSize,
      final Map<ConstraintTypeEnum, String> constraints) {
    final List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(
        fieldValue -> fieldValue.matches("\\$\\{\\w*}") ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) : fieldValue);

    final var value = new HashMap<>(mapSize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value.put(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()),
                  RandomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
      } else if ("it".equals(fieldType)) {
        if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomIterator.isTypeSupported(fieldType))) {
          value.put(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()),
                    RandomIterator.generateIteratorForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
        } else {
          for (int i = mapSize; i > 0; i--) {
            value.put(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()),
                      RandomIterator.generateIt(fieldName, fieldType, parameterList, CONTEXT));
          }
        }
      } else {
        for (int i = mapSize; i > 0; i--) {
          value.put(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()),
                    RandomSequence.generateSeq(fieldName, fieldType, parameterList, CONTEXT));
        }
      }
    } else {
      return RANDOM_MAP.generateMap(fieldType, mapSize, parameterList, valueLength, mapSize, constraints);
    }

    return value;
  }
}
