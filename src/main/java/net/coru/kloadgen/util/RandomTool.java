package net.coru.kloadgen.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public final class RandomTool {

  public static Object generateRandom(String valueExpression) {
    Object value = valueExpression;
    switch (valueExpression) {
      case "string":
        value = RandomStringUtils.randomAlphabetic(20);
        break;
      case "int":
        value = RandomUtils.nextInt();
        break;
      case "long":
      case "union":
        value = RandomUtils.nextLong();
        break;
      case "timestamp":
        value = LocalDateTime.now();
        break;
      case "longTimestamp":
        value = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        break;
      case "stringTimestamp":
        value = LocalDateTime.now().toString();
        break;
      case "short":
        value = RandomUtils.nextInt(0, 32767);
        break;
      case "uuid":
        value = UUID.randomUUID().toString();
        break;
    }
    return value;
  }
  public static Object generateRandom(String valueExpression, Field field) {

    Object value = generateRandom(valueExpression);
    if ("ENUM".equalsIgnoreCase(field.schema().getType().getName())) {
       if ("ENUM".equalsIgnoreCase(valueExpression)) {
         List<String> enumValueList= field.schema().getEnumSymbols();
         value = enumValueList.get(RandomUtils.nextInt(0, enumValueList.size()));
       } else {
         value = new GenericData.EnumSymbol(field.schema(), valueExpression);
       }
    } else if (valueExpression.equalsIgnoreCase(value.toString())) {
      switch (field.schema().getType().getName().toUpperCase()) {
        case "INT":
          value = Integer.valueOf(valueExpression);
          break;
        case "LONG":
        case "UNION":
          value = Long.valueOf(valueExpression);
          break;
        case "SHORT":
          value = Short.valueOf(valueExpression);
          break;
        default:
          value = valueExpression;
          break;
      }
    }
    return value;
  }
}
