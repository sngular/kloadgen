package net.coru.kloadgen.randomtool.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.util.ValidType;
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
      case ValidType.INT_ARRAY:
        value = generate(ValidType.INT, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidType.LONG_ARRAY:
        value = generate(ValidType.LONG, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidType.DOUBLE_ARRAY:
        value = generate(ValidType.DOUBLE, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidType.SHORT_ARRAY:
        value = generate(ValidType.SHORT, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidType.FLOAT_ARRAY:
        value = generate(ValidType.FLOAT, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidType.STRING_ARRAY:
        value = generate(ValidType.STRING, arraySize, valueLength, fieldValueList, constrains);
        break;
      case ValidType.UUID_ARRAY:
        value = generate(ValidType.UUID, arraySize, 0, fieldValueList, Collections.emptyMap());
        break;
      case ValidType.BOOLEAN_ARRAY:
        value = generate(ValidType.BOOLEAN, arraySize, 0, fieldValueList, Collections.emptyMap());
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
