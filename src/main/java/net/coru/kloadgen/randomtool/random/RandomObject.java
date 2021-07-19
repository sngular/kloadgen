package net.coru.kloadgen.randomtool.random;

import com.github.curiousoddman.rgxgen.RgxGen;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.util.ValidTypes;
import net.coru.kloadgen.randomtool.util.Utils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class RandomObject {

  public boolean isTypeValid(String type) {
    return ValidTypes.VALID_OBJECT_TYPES.contains(type);
  }

  public Object generateSeq(String fieldName, String fieldType, List<String> fieldValueList, Map<String, Object> context) {
    return Utils.castValue(
        context.compute(fieldName, (fieldNameMap,
            seqObject) -> seqObject == null ? getSafeValue(fieldValueList) : ((Long) seqObject) + 1),
        fieldType);
  }

  public Object generateRandom(String fieldType, Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    Object value;
    switch (fieldType) {
      case ValidTypes.STRING:
        value = getStringValueOrRandom(valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.NUMBER:
        value = getNumberValueOrRandom(valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.INT:
        value = getIntValueOrRandom(valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.LONG:
        value = getLongValueOrRandom(valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.SHORT:
        value = getShortValueOrRandom(valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.DOUBLE:
        value = getDoubleValueOrRandom(valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.FLOAT:
        value = getFloatValueOrRandom(valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.BYTES:
        value = getByteRandom(valueLength);
        break;
      case ValidTypes.TIMESTAMP:
      case ValidTypes.LONG_TIMESTAMP:
      case ValidTypes.STRING_TIMESTAMP:
        value = getTimestampValueOrRandom(fieldType, fieldValueList);
        break;
      case ValidTypes.UUID:
        value = getUUIDValueOrRandom(fieldValueList);
        break;
      case ValidTypes.BOOLEAN:
        value = getBooleanValueOrRandom(fieldValueList);
        break;
      case ValidTypes.ENUM:
        value = getEnumValueOrRandom(fieldValueList);
        break;
      default:
        value = fieldType;
        break;
    }
    return value;
  }

  private Long getSafeValue(List<String> fieldValueList) {
    return fieldValueList.isEmpty() ? 1L : Long.parseLong(fieldValueList.get(0));
  }

  private Integer getIntValueOrRandom(Integer valueLength, List<String> fieldValueList, Map<ConstraintTypeEnum, String> constrains) {
    int value;
    if (!fieldValueList.isEmpty()) {
      value = Integer.parseInt(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      Number minimum = calculateMinimum(valueLength, constrains);
      Number maximum = calculateMaximum(valueLength, constrains);
      if (Integer.MAX_VALUE < ((BigDecimal) maximum).longValueExact()) {
        maximum = Integer.MAX_VALUE;
      }
      if (Integer.MAX_VALUE < minimum.longValue()) {
        minimum = Integer.MIN_VALUE;
      }
      if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
        int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
        maximum = maximum.intValue() > multipleOf ? maximum.intValue() / multipleOf : maximum;
        value = RandomUtils.nextInt(minimum.intValue(), maximum.intValue()) * multipleOf;
      } else {
        value = RandomUtils.nextInt(minimum.intValue(), maximum.intValue());
      }
    }
    return value;
  }

  private Number getNumberValueOrRandom(Integer valueLength, List<String> fieldValueList, Map<ConstraintTypeEnum, String> constrains) {
    Number value;
    if (!fieldValueList.isEmpty()) {
      String chosenValue = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
      if (chosenValue.contains(".")) {
        value = Float.parseFloat(chosenValue);
      } else {
        value = Integer.parseInt(chosenValue);
      }
    } else {
      Number minimum = calculateMinimum(valueLength, constrains);
      Number maximum = calculateMaximum(valueLength, constrains);
      if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
        int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
        maximum = maximum.intValue() > multipleOf ? maximum.intValue() / multipleOf : maximum;
        value = RandomUtils.nextFloat(minimum.intValue(), maximum.intValue()) * multipleOf;
      } else {
        value = RandomUtils.nextFloat(minimum.intValue(), maximum.intValue());
      }
    }
    return value;
  }

  private Long getLongValueOrRandom(Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    long value;
    if (!fieldValueList.isEmpty()) {
      value = Long.parseLong(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      value = getRandomLongValue(valueLength, constrains);
    }
    return value;
  }

  private long getRandomLongValue(Integer valueLength, Map<ConstraintTypeEnum, String> constrains) {
    long value;
    Number minimum = calculateMinimum(valueLength, constrains);
    Number maximum = calculateMaximum(valueLength, constrains);
    if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
      int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
      maximum = maximum.longValue() > multipleOf ? maximum.longValue() / multipleOf : maximum;
      value = RandomUtils.nextLong(minimum.longValue(), maximum.longValue()) * multipleOf;
    } else {
      value = RandomUtils.nextLong(minimum.longValue(), maximum.longValue());
    }
    return value;
  }

  private Double getDoubleValueOrRandom(Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    double value;
    if (!fieldValueList.isEmpty()) {
      value = Double.parseDouble(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      long intPart = getRandomLongValue(valueLength / 2, constrains);
      long decPart = getRandomLongValue(valueLength / 2, constrains);
      value = Double.parseDouble(intPart + "." + decPart);
    }
    return value;
  }

  private Float getFloatValueOrRandom(Integer valueLength, List<String> fieldValuesList,
      Map<ConstraintTypeEnum, String> constrains) {
    float value;
    if (!fieldValuesList.isEmpty()) {
      value = Float.parseFloat(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
    } else {
      long intPart = getRandomLongValue(valueLength / 2, constrains);
      long decPart = getRandomLongValue(valueLength / 2, constrains);
      value = new BigDecimal(intPart + "." + decPart).floatValue();
    }
    return value;
  }

  private ByteBuffer getByteRandom(Integer valueLength) {
    ByteBuffer value;
    if (valueLength == 0) {
      value = ByteBuffer.wrap(RandomUtils.nextBytes(4));
    } else {
      value = ByteBuffer.wrap(RandomUtils.nextBytes(valueLength));
    }
    return value;
  }

  private String getStringValueOrRandom(Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    String value;
    if (!fieldValueList.isEmpty()) {
      value = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
    } else {
      if (constrains.containsKey(ConstraintTypeEnum.REGEX)) {
        RgxGen rxGenerator = new RgxGen(constrains.get(ConstraintTypeEnum.REGEX));
        value = rxGenerator.generate();
        if (valueLength > 0 || constrains.containsKey(ConstraintTypeEnum.MAXIMUM_VALUE)) {
          value = value.substring(0, getMaxLength(valueLength, constrains.get(ConstraintTypeEnum.MAXIMUM_VALUE)));
        }
      } else {
        value = RandomStringUtils.randomAlphabetic(valueLength == 0 ? RandomUtils.nextInt(1, 20) : valueLength);
      }    }
    return value;
  }

  private int getMaxLength(Integer valueLength, String maxValueStr) {
    int maxValue = Integer.parseInt(StringUtils.defaultIfEmpty(maxValueStr, "0"));
    if (valueLength > 0 && maxValue == 0 ) {
      maxValue = valueLength;
    }
    return maxValue;
  }

  private Short getShortValueOrRandom(Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    short value;
    if (!fieldValueList.isEmpty()) {
      value = Short.parseShort(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      if (valueLength < 5 ) {
        Number minimum = calculateMinimum(valueLength, constrains);
        Number maximum = calculateMaximum(valueLength, constrains);
        if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
          int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
          maximum = maximum.shortValue() > multipleOf ? maximum.shortValue() / multipleOf : maximum;
          value = (short) (RandomUtils.nextInt(minimum.shortValue(), maximum.shortValue()) * multipleOf);
        } else {
          value = (short) RandomUtils.nextInt(minimum.shortValue(), maximum.shortValue());
        }
      } else {
        value = (short) RandomUtils.nextInt(1, 32767);
      }
    }
    return value;
  }

  private Object getTimestampValueOrRandom(String type, List<String> fieldValueList) {
    LocalDateTime value;
    if (!fieldValueList.isEmpty()) {
      value = LocalDateTime.parse(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      value = LocalDateTime.now();
    }
    if ("longTimestamp".equalsIgnoreCase(type)) {
      return value.toInstant(ZoneOffset.UTC).toEpochMilli();
    } else if ("stringTimestamp".equalsIgnoreCase(type)) {
      return value.toString();
    }
    return value;
  }

  private UUID getUUIDValueOrRandom(List<String> fieldValueList) {
    UUID value = UUID.randomUUID();
    if (!fieldValueList.isEmpty()) {
      value = UUID.fromString(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    }
    return value;
  }

  private Boolean getBooleanValueOrRandom(List<String> fieldValueList) {
    boolean value = RandomUtils.nextBoolean();
    if (!fieldValueList.isEmpty()) {
      value = Boolean.parseBoolean(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    }
    return value;
  }

  private String getEnumValueOrRandom(List<String> fieldValueList) {
    String value;
    if (!fieldValueList.isEmpty()) {
      value = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
    } else {
      throw new KLoadGenException("Wrong enums values, problem in the parsing process");
    }
    return value;
  }

  private Number calculateMaximum(int valueLength, Map<ConstraintTypeEnum, String> constrains) {
    Number maximum;
    if (constrains.containsKey(ConstraintTypeEnum.MAXIMUM_VALUE)) {
      if (constrains.containsKey(ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE)) {
        maximum = Integer.parseInt(constrains.get(ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE)) - 1L;
      } else {
        maximum = Integer.parseInt(constrains.get(ConstraintTypeEnum.MAXIMUM_VALUE));
      }
    } else {
      maximum = new  BigDecimal(StringUtils.rightPad("1", valueLength + 1, '0'));
    }
    return maximum;
  }

  private Number calculateMinimum(int valueLength, Map<ConstraintTypeEnum, String> constrains) {
    Number minimum;
    if (constrains.containsKey(ConstraintTypeEnum.MINIMUM_VALUE)) {
      if (constrains.containsKey(ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE)) {
        minimum = Integer.parseInt(constrains.get(ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE)) - 1;
      } else {
        minimum = Integer.parseInt(constrains.get(ConstraintTypeEnum.MINIMUM_VALUE));
      }
    } else {
      minimum = Long.parseLong(StringUtils.rightPad("1", valueLength, '0'));
    }
    return minimum;
  }
}
