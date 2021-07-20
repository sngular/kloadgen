package net.coru.kloadgen.randomtool.random;

import com.github.curiousoddman.rgxgen.RgxGen;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.util.Utils;
import net.coru.kloadgen.randomtool.util.ValidTypes;
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
      case ValidTypes.INT:
        try {
          value = getIntegerValueOrRandom(valueLength, fieldValueList, constrains).intValueExact();
        } catch (ArithmeticException exception) {
          value = Integer.MAX_VALUE;
        }
        break;
      case ValidTypes.LONG:
        try {
          value = getIntegerValueOrRandom(valueLength, fieldValueList, constrains).longValueExact();
        } catch (ArithmeticException exception) {
          value = Long.MAX_VALUE;
        }
        break;
      case ValidTypes.SHORT:
        try {
          value = getIntegerValueOrRandom(valueLength, fieldValueList, constrains).shortValueExact();
        } catch (ArithmeticException exception) {
          value = Short.MAX_VALUE;
        }
        break;
      case ValidTypes.DOUBLE:
        try {
          value = getDecimalValueOrRandom(valueLength, fieldValueList, constrains).doubleValue();
        } catch (ArithmeticException exception) {
          value = Double.MAX_VALUE;
        }
        break;
      case ValidTypes.FLOAT:
        try {
          value = getDecimalValueOrRandom(valueLength, fieldValueList, constrains).floatValue();
        } catch (ArithmeticException exception) {
          value = Float.MAX_VALUE;
        }
        break;
      case ValidTypes.BYTES:
        try {
          value = getIntegerValueOrRandom(valueLength, Collections.emptyList(), Collections.emptyMap()).byteValueExact();
        } catch (ArithmeticException exception) {
          value = Byte.MAX_VALUE;
        }
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

  private BigInteger getIntegerValueOrRandom(Integer valueLength, List<String> fieldValueList, Map<ConstraintTypeEnum, String> constrains) {
    BigInteger value;

    if (!fieldValueList.isEmpty()) {
      value = new BigInteger(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());

    } else {
      Number minimum = calculateMinimum(valueLength, constrains);
      Number maximum = calculateMaximum(valueLength, constrains);

      if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
        int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
        maximum = maximum.intValue() > multipleOf ? maximum.intValue() / multipleOf : maximum;
        value = BigInteger.valueOf(RandomUtils.nextLong(minimum.longValue(), maximum.longValue()) * multipleOf);
      } else {
        value = BigInteger.valueOf(RandomUtils.nextLong(minimum.longValue(), maximum.longValue()));
      }
    }

    return value;
  }

  private BigDecimal getDecimalValueOrRandom(Integer valueLength, List<String> fieldValueList, Map<ConstraintTypeEnum, String> constrains) {
    BigDecimal value;

    if (!fieldValueList.isEmpty()) {
      value = new BigDecimal(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
    } else {
      Number minimum = calculateMinimum(valueLength - 1, constrains);
      Number maximum = calculateMaximum(valueLength - 1, constrains);

      if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
        int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
        maximum = maximum.intValue() > multipleOf ? maximum.intValue() / multipleOf : maximum;
        value = BigDecimal.valueOf(RandomUtils.nextDouble(minimum.doubleValue(), maximum.doubleValue()) * multipleOf);
      } else {
        if (valueLength < 3) {
          value = new BigDecimal(getIntegerValueOrRandom(valueLength, fieldValueList, constrains));
        } else {
          BigDecimal aux = BigDecimal.valueOf(RandomUtils.nextLong(minimum.longValue(), maximum.longValue()));
          int decLength = RandomUtils.nextInt(1, valueLength / 2);
          value = aux.multiply(BigDecimal.valueOf(0.1).pow(decLength));
        }
      }
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
      }
    }
    return value;
  }

  private int getMaxLength(Integer valueLength, String maxValueStr) {
    int maxValue = Integer.parseInt(StringUtils.defaultIfEmpty(maxValueStr, "0"));
    if (valueLength > 0 && maxValue == 0) {
      maxValue = valueLength;
    }
    return maxValue;
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
        maximum = Long.parseLong(constrains.get(ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE)) - 1L;
      } else {
        maximum = Long.parseLong(constrains.get(ConstraintTypeEnum.MAXIMUM_VALUE));
      }
    } else {
      maximum = new BigDecimal(StringUtils.rightPad("9", valueLength, '0'));
    }
    return maximum;
  }

  private Number calculateMinimum(int valueLength, Map<ConstraintTypeEnum, String> constrains) {
    Number minimum;
    if (constrains.containsKey(ConstraintTypeEnum.MINIMUM_VALUE)) {
      if (constrains.containsKey(ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE)) {
        minimum = Long.parseLong(constrains.get(ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE)) - 1;
      } else {
        minimum = Long.parseLong(constrains.get(ConstraintTypeEnum.MINIMUM_VALUE));
      }
    } else {
      minimum = Long.parseLong(StringUtils.rightPad("1", valueLength, '0'));
    }
    return minimum;
  }
}
