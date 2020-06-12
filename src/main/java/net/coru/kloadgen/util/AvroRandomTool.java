package net.coru.kloadgen.util;

import static org.apache.avro.Schema.Type.ENUM;
import static org.apache.avro.Schema.Type.FIXED;
import static org.apache.avro.Schema.Type.NULL;
import static org.apache.avro.Schema.Type.UNION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericFixed;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.jmeter.threads.JMeterContextService;

public class AvroRandomTool {

  private final Map<String, Object> context = new HashMap<>();

  public Object generateRandomMap(String fieldType, Integer valueLength, List<String> fieldValuesList, Field field, Integer size) {

    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
        fieldValue.matches("\\$\\{\\w*}") ?
            JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
            fieldValue
    );

    return RandomTool.generateRandomMap(fieldType, valueLength, parameterList, size);

  }

  public Object generateRandom(String fieldType, Integer valueLength, List<String> fieldValuesList, Field field) {

    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
        fieldValue.matches("\\$\\{\\w*}") ?
            JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
            fieldValue
    );

    Object value = RandomTool.generateRandom(fieldType, valueLength, parameterList);

    if (ENUM == field.schema().getType()) {
      value = getEnumOrGenerate(fieldType, field.schema(),parameterList);
    } else if (UNION == field.schema().getType()) {
      Schema safeSchema = getRecordUnion(field.schema().getTypes());
      if ("null".equalsIgnoreCase(value.toString())) {
        value = null;
      } else if (differentTypesNeedCast(fieldType, safeSchema.getType())) {
        value = RandomTool.castValue(value, field.schema().getType().getName());
      } else if (ENUM == safeSchema.getType()) {
        value = getEnumOrGenerate(fieldType, safeSchema,parameterList);
      }
    } else if ("seq".equalsIgnoreCase(value.toString())) {
      value = RandomTool.generateSeq(field.name(), field.schema().getType().getName(), parameterList, context);
    } else if (differentTypesNeedCast(fieldType, field.schema().getType())) {
      value = RandomTool.castValue(value, field.schema().getType().getName());
    }
	if(FIXED == field.schema().getType()) {
		value = getFixedOrGenerate(field.schema());
	}
    return value;
  }

  private static boolean differentTypesNeedCast(String fieldType, Type fieldTypeSchema) {

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

  private static GenericFixed getFixedOrGenerate( Schema schema) {

    byte[] bytes = new byte[schema.getFixedSize()];

		return new GenericData.Fixed(schema, bytes);
	}

  private static boolean needCastForInt(String fieldType) {

    switch (fieldType) {
      case "short":
      case "int":
        return false;
      default:
        return !Type.INT.getName().equals(fieldType);
    }
  }

  private static boolean needCastForString(String fieldType) {

    switch (fieldType) {
      case "timestamp":
      case "uuid":
        return false;
      default:
        return !Type.STRING.getName().equals(fieldType);
    }
  }

  private static Object getEnumOrGenerate(String fieldType, Schema schema,List<String> parameterList) {
    Object value;
    if ("ENUM".equalsIgnoreCase(fieldType)) {
      if(parameterList.isEmpty()) {
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
