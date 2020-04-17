package net.coru.kloadgen.util;

import static org.apache.avro.Schema.Type.ENUM;
import static org.apache.avro.Schema.Type.UNION;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.commons.lang3.RandomUtils;

public class AvroRandomTool {

  private Map<String, Object> context = new HashMap<>();

  public Object generateRandom(String fieldType, Integer valueLength, List<String> fieldValuesList, Field field) {

    Object value = RandomTool.generateRandom(fieldType, valueLength, fieldValuesList);

    if (ENUM == field.schema().getType()) {
      value = getEnumOrGenerate(fieldType, field.schema());
    } else if (UNION == field.schema().getType()) {
      value = ("null".equalsIgnoreCase(value.toString())) ? null : getEnumOrGenerate(fieldType, field.schema().getTypes().get(1));
    } else if ("seq".equals(value)) {
      value = RandomTool.generateSeq(field.name(), field.schema().getType().getName(), fieldValuesList, context);
    } else if (diferentTypesNeedCast(fieldType, field.schema().getType())) {
      value = RandomTool.castValue(value, field.schema().getType().getName());
    }
    return value;
  }



  private static boolean diferentTypesNeedCast(String fieldType, Type fieldTypeSchema) {

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

  private static Object getEnumOrGenerate(String fieldType, Schema schema) {
    Object value;
    if ("ENUM".equalsIgnoreCase(fieldType)) {
      List<String> enumValueList = schema.getEnumSymbols();
      value = new GenericData.EnumSymbol(schema, enumValueList.get(RandomUtils.nextInt(0, enumValueList.size())));
    } else {
      value = new GenericData.EnumSymbol(schema, fieldType);
    }
    return value;
  }

}
