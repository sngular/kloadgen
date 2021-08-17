/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.generator;

import static org.apache.avro.Schema.Type.ENUM;
import static org.apache.avro.Schema.Type.FIXED;
import static org.apache.avro.Schema.Type.NULL;
import static org.apache.avro.Schema.Type.UNION;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
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

  private final RandomMap randomMap = new RandomMap();

  private final RandomArray randomArray = new RandomArray();

  private final RandomObject randomObject = new RandomObject();

  public Object generateMap(String fieldType, Integer valueLength, List<String> fieldValuesList, Integer size) {
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    return randomMap.generateMap(fieldType, valueLength, parameterList, size, Collections.emptyMap());
  }

  public Object generateArray(String fieldType, Integer arraySize, Integer valueLength, List<String> fieldValuesList) {
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    return randomArray.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
  }

  public Object generateObject(Field field, String fieldType, Integer valueLength, List<String> fieldValuesList) {
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);

    Object value;
    if (ENUM == field.schema().getType()) {
      value = getEnumOrGenerate(fieldType, field.schema(), parameterList);
    } else if (UNION == field.schema().getType()) {
      Schema safeSchema = getRecordUnion(field.schema().getTypes());
      if (differentTypesNeedCast(fieldType, safeSchema.getType())) {

        value = randomObject.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
        value = ValueUtils.castValue(value, field.schema().getType().getName());
      } else if (ENUM == safeSchema.getType()) {
        value = getEnumOrGenerate(fieldType, safeSchema, parameterList);
      } else {
        value = randomObject.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
        if ("null".equalsIgnoreCase(value.toString())) {
          value = null;
        }
      }
    } else if ("seq".equalsIgnoreCase(fieldType)) {
      value = randomObject.generateSeq(field.name(), field.schema().getType().getName(), parameterList, context);
    } else if (differentTypesNeedCast(fieldType, field.schema().getType())) {

      value = randomObject.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
      value = ValueUtils.castValue(value, field.schema().getType().getName());
    } else if (FIXED == field.schema().getType()) {
      value = getFixedOrGenerate(field.schema());
    } else {
      value = randomObject.generateRandom(fieldType, valueLength, parameterList, Collections.emptyMap());
    }
    return value;
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
        return !fieldTypeSchema.getName().equals(fieldType);
    }
  }

  private GenericFixed getFixedOrGenerate(Schema schema) {

    byte[] bytes = new byte[schema.getFixedSize()];

    return new GenericData.Fixed(schema, bytes);
  }

  private boolean needCastForInt(String fieldType) {
    switch (fieldType) {
      case "short":
      case "int":
        return false;
      default:
        return !Type.INT.getName().equals(fieldType);
    }
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

  private Object getEnumOrGenerate(String fieldType, Schema schema, List<String> parameterList) {
    Object value;
    if ("ENUM".equalsIgnoreCase(fieldType)) {
      if (parameterList.isEmpty()) {
        List<String> enumValueList = schema.getEnumSymbols();
        value = new GenericData.EnumSymbol(schema, enumValueList.get(RandomUtils.nextInt(0, enumValueList.size())));
      } else {
        value = new GenericData.EnumSymbol(schema, parameterList.get(RandomUtils.nextInt(0, parameterList.size())));
      }
    } else {
      value = new GenericData.EnumSymbol(schema, fieldType);
    }
    return value;
  }

  private Schema getRecordUnion(List<Schema> types) {
    return IterableUtils.find(types, schema -> !schema.getType().equals(NULL));
  }
}
