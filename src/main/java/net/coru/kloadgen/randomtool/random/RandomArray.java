package net.coru.kloadgen.randomtool.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.util.ValidTypes;
import org.apache.commons.lang3.RandomUtils;

public class RandomArray {

  private final RandomObject randomObject;

  public RandomArray() {
    randomObject = new RandomObject();
  }

  public Object generateArray(String fieldType, Integer valueLength, List<String> fieldValueList, Integer arraySize,
      Map<ConstraintTypeEnum, String> constrains) {
    Object value;

    switch (fieldType) {
      case ValidTypes.INT_ARRAY:
        value = generate(ValidTypes.INT, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.LONG_ARRAY:
        value = generate(ValidTypes.LONG, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.DOUBLE_ARRAY:
        value = generate(ValidTypes.DOUBLE, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.SHORT_ARRAY:
        value = generate(ValidTypes.SHORT, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.FLOAT_ARRAY:
        value = generate(ValidTypes.FLOAT, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.STRING_ARRAY:
        value = generate(ValidTypes.STRING, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidTypes.UUID_ARRAY:
        value = generate(ValidTypes.UUID, arraySize, 0, fieldValueList, Collections.emptyMap());
        break;
      case ValidTypes.BOOLEAN_ARRAY:
        value = generate(ValidTypes.BOOLEAN, arraySize, 0, fieldValueList, Collections.emptyMap());
        break;
      default:
        value = new ArrayList<>();
        break;
    }

    return value;
  }

  private List<Object> generate(String type, Integer arraySize, Integer valueLength, List<String> fieldValueList,
      Map<ConstraintTypeEnum, String> constrains) {
    int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
    List<Object> array = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      array.add(randomObject.generateRandom(type, valueLength, fieldValueList, constrains));
    }

    return array;
  }
}
