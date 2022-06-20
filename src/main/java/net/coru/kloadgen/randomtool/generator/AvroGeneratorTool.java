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

  public Object generateObject(final Field field, final FieldValueMapping fieldValueMapping, final Map<ConstraintTypeEnum, String> constraints) {
    final String fieldType = fieldValueMapping.getFieldType();
    final Integer valueLength = fieldValueMapping.getValueLength();
    final List<String> fieldValuesList = fieldValueMapping.getFieldValuesList();

    final List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    final boolean logicalType = Objects.nonNull(field.schema().getLogicalType());

    Object value;
    if (Type.ENUM == field.schema().getType() && !"seq".equalsIgnoreCase(fieldType) && !"it".equalsIgnoreCase(fieldType)) {
      value = getEnumOrGenerate(fieldValueMapping.getFieldName(), fieldType, field.schema(), parameterList, field.schema().getType().getName());
    } else if (Type.UNION == field.schema().getType() && !"seq".equalsIgnoreCase(fieldType) && !"it".equalsIgnoreCase(fieldType)) {
      final Schema safeSchema = getRecordUnion(field.schema().getTypes());
      if (differentTypesNeedCast(fieldType, safeSchema.getType())) {

        value = randomObject.generateRandom(fieldType, valueLength, parameterList, constraints);
        value = ValueUtils.castValue(value, field.schema().getType().getName());
      } else if (Type.ENUM == safeSchema.getType()) {
        value = getEnumOrGenerate(fieldValueMapping.getFieldName(), fieldType, safeSchema, parameterList, field.schema().getType().getName());
      } else {
        value = randomObject.generateRandom(fieldType, valueLength, parameterList, constraints);
        if ("null".equalsIgnoreCase(value.toString())) {
          value = null;
        }
      }
    } else if ("seq".equalsIgnoreCase(fieldType)) {
      final String type = Type.UNION.getName().equals(ValueUtils.getValidTypeFromSchema(field.schema())) ? getRecordUnion(field.schema().getTypes()).getName()
          : ValueUtils.getValidTypeFromSchema(field.schema());
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
        value = RandomIterator.generateIt(field.name(), type, parameterList, context);
      }
    } else if (differentTypesNeedCast(fieldType, field.schema().getType())) {

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
}
