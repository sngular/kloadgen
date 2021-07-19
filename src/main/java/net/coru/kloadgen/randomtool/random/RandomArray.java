package net.coru.kloadgen.randomtool.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.util.ValidTypes;
import org.apache.commons.lang3.RandomUtils;

public class RandomArray {

  private final RandomObject randomObject;

  public RandomArray() {
    randomObject  = new RandomObject();
  }

  public Object generateArray(String fieldType, Integer valueLength, List<String> fieldValueList, Integer arraySize,
      Map<ConstraintTypeEnum, String> constrains) {
    Object value;
    switch (fieldType) {
      case ValidTypes.INT_ARRAY:
        value = generateIntArray(arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.NUMBER_ARRAY:
        value = generateNumberArray(arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.LONG_ARRAY:
        value = generateLongArray(arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.DOUBLE_ARRAY:
        value = generateDoubleArray(arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.SHORT_ARRAY:
        value = generateShortArray(arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.FLOAT_ARRAY:
        value = generateFloatArray(arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.STRING_ARRAY:
        value = generateStringArray(arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.UUID_ARRAY:
        value = generateUuidArray(arraySize, fieldValueList);
        break;
      case ValidTypes.BOOLEAN_ARRAY:
        value = generateBooleanArray(arraySize, fieldValueList);
        break;
      default:
        value = new ArrayList<>();
        break;
    }
    return value;
  }

  private List<Integer> generateIntArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Integer> intArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      intArray.add(
          (Integer) randomObject.generateRandom(ValidTypes.INT, valueLength, fieldValueList, constrains)
      );
    }
    return intArray;
  }

  private List<Number> generateNumberArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Number> intArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      intArray.add(
          (Integer) randomObject.generateRandom(ValidTypes.NUMBER, valueLength, fieldValueList, constrains)
      );
    }
    return intArray;
  }

  private List<Long> generateLongArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Long> longArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      longArray.add(
          (Long) randomObject.generateRandom(ValidTypes.LONG, valueLength, fieldValueList, constrains)
      );
    }
    return longArray;
  }

  private List<Double> generateDoubleArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Double> doubleArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      doubleArray.add(
          (Double) randomObject.generateRandom(ValidTypes.DOUBLE, valueLength, fieldValueList, constrains)
      );
    }
    return doubleArray;
  }

  private List<Short> generateShortArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
    List<Short> shortArray = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      shortArray.add(
          (Short) randomObject.generateRandom(ValidTypes.SHORT, valueLength, fieldValueList, constrains)
      );
    }
    return shortArray;
  }

  private List<Float> generateFloatArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
    List<Float> floatArray = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      floatArray.add(
          (Float) randomObject.generateRandom(ValidTypes.FLOAT, valueLength, fieldValueList, constrains)
      );
    }
    return floatArray;
  }

  private List<String> generateStringArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
    List<String> stringArray = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      stringArray.add(
          (String) randomObject.generateRandom(ValidTypes.STRING, valueLength, fieldValueList, constrains)
      );
    }
    return stringArray;
  }

  private List<UUID> generateUuidArray(Integer arraySize, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<UUID> uuidArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      uuidArray.add(
          (UUID) randomObject.generateRandom(ValidTypes.UUID, 0, fieldValueList, Collections.emptyMap())
      );
    }
    return uuidArray;
  }

  private List<Boolean> generateBooleanArray(Integer arraySize, List<String> fieldValueList) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1,5) : arraySize;
    List<Boolean> booleanArray = new ArrayList<>();
    for (int i=0; i<size; i++) {
      booleanArray.add(
          (Boolean) randomObject.generateRandom(ValidTypes.BOOLEAN, 0, fieldValueList, Collections.emptyMap())
      );
    }
    return booleanArray;
  }
}
