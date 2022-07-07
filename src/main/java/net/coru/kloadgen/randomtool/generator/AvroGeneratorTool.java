/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.generator;

import static net.coru.kloadgen.randomtool.util.ValueUtils.getValidTypeFromSchema;
import static org.apache.avro.Schema.Type.ENUM;
import static org.apache.avro.Schema.Type.FIXED;
import static org.apache.avro.Schema.Type.NULL;
import static org.apache.avro.Schema.Type.UNION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.random.RandomArray;
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

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  private static final RandomArray RANDOM_ARRAY = new RandomArray();

  private static final RandomMap RANDOM_MAP = new RandomMap();

  private static final Map<String, Object> CONTEXT = new HashMap<>();

  public Object generateObject(Schema schema, FieldValueMapping fieldValueMapping, Map<ConstraintTypeEnum, String> constraints) {
    return generateObject(schema, fieldValueMapping.getFieldName(), fieldValueMapping.getFieldType(), fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList(),
                          constraints);
  }

  public Object generateObject(
      Schema schema, String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList, Map<ConstraintTypeEnum, String> constraints) {

    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    boolean logicalType = Objects.nonNull(schema.getLogicalType());

    Object value;
    if (ENUM == schema.getType() && !"seq".equalsIgnoreCase(fieldType)) {
      value = getEnumOrGenerate(fieldName, fieldType, schema, parameterList, schema.getType().getName());
    } else if (UNION == schema.getType() && !"seq".equalsIgnoreCase(fieldType)) {
      Schema safeSchema = getRecordUnion(schema.getTypes());
      if (differentTypesNeedCast(fieldType, safeSchema.getType())) {

        value = RANDOM_OBJECT.generateRandom(fieldType, valueLength, parameterList, constraints);
        value = ValueUtils.castValue(value, schema.getType().getName());
      } else if (ENUM == safeSchema.getType()) {
        value = getEnumOrGenerate(fieldName, fieldType, safeSchema, parameterList, schema.getType().getName());
      } else {
        value = RANDOM_OBJECT.generateRandom(fieldType, valueLength, parameterList, constraints);
        if ("null".equalsIgnoreCase(value.toString())) {
          value = null;
        }
      }
    } else if ("seq".equalsIgnoreCase(fieldType)) {
      String type = UNION.getName().equals(getValidTypeFromSchema(schema)) ? getRecordUnion(schema.getTypes()).getName() : getValidTypeFromSchema(schema);
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(type))) {
        return RandomSequence.generateSequenceForFieldValueList(fieldName, type, fieldValuesList, CONTEXT);
      } else {
        value = RandomSequence.generateSeq(fieldName, type, parameterList, CONTEXT);
      }
    } else if (differentTypesNeedCast(fieldType, schema.getType())) {

      value = RANDOM_OBJECT.generateRandom(fieldType, valueLength, parameterList, constraints);
      value = ValueUtils.castValue(value, schema.getType().getName());
    } else if (!logicalType && FIXED == schema.getType()) {
      value = getFixedOrGenerate(schema);
    } else {
      value = RANDOM_OBJECT.generateRandom(fieldType, valueLength, parameterList, constraints);
    }
    return value;
  }

  private Object getEnumOrGenerate(String fieldName, String fieldType, Schema schema, List<String> parameterList, String fieldValueMappingType) {
    Object value;
    if ("ENUM".equalsIgnoreCase(fieldValueMappingType)) {
      if (parameterList.isEmpty()) {
        List<String> enumValueList = schema.getEnumSymbols();
        value = new GenericData.EnumSymbol(schema, enumValueList.get(RandomUtils.nextInt(0, enumValueList.size())));
      } else {
        if ("Seq".equalsIgnoreCase(fieldType)) {
          value = new GenericData.EnumSymbol(schema, RandomSequence.generateSequenceForFieldValueList(fieldName, fieldValueMappingType, parameterList, CONTEXT));
        } else {
          value = new GenericData.EnumSymbol(schema, parameterList.get(RandomUtils.nextInt(0, parameterList.size())));
        }
      }
    } else {
      value = new GenericData.EnumSymbol(schema, fieldType);
    }
    return value;
  }

  private Schema getRecordUnion(List<Schema> types) {
    return IterableUtils.find(types, schema -> !schema.getType().equals(NULL));
  }

  private boolean differentTypesNeedCast(String fieldType, Type fieldTypeSchema) {
    switch (fieldTypeSchema) {
      case RECORD:
      case ENUM:
      case ARRAY:
      case MAP:
      case UNION:
      case FIXED:
      case NULL:
        return false;
      case STRING:
        return needCastForString(fieldType);
      case INT:
        return needCastForInt(fieldType);
      case LONG:
      case FLOAT:
      case DOUBLE:
      case BOOLEAN:
      case BYTES:
      default:
        return !fieldTypeSchema.getName().equals(fieldType.split("_")[0]);
    }
  }

  private GenericFixed getFixedOrGenerate(Schema schema) {

    byte[] bytes = new byte[schema.getFixedSize()];

    return new GenericData.Fixed(schema, bytes);
  }

  private boolean needCastForString(String fieldType) {
    switch (fieldType) {
      case "timestamp":
      case "uuid":
        return false;
      default:
        return !Type.STRING.getName().equals(fieldType);
    }
  }

  private boolean needCastForInt(String fieldType) {
    switch (fieldType) {
      case "short":
      case "int":
        return false;
      default:
        return !Type.INT.getName().equals(fieldType.split("_")[0]);
    }
  }

  public Object generateArray(
      String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList, Integer arraySize,
      Map<ConstraintTypeEnum, String> constraints) {
    final List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(
        fieldValue -> fieldValue.matches("\\$\\{\\w*}") ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) : fieldValue);

    final List value = new ArrayList<>(valueLength);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value.add(RandomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
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
      String fieldName, String fieldType, Integer valueLength, List<String> fieldValuesList, Integer mapSize,
      Map<ConstraintTypeEnum, String> constraints) {
    final List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}")
                                     ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                                                                                                                                  fieldValue
    );

    final var value = new HashMap<>(mapSize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value.put(RANDOM_OBJECT.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()),
                  RandomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
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
