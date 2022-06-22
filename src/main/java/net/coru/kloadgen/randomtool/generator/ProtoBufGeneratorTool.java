package net.coru.kloadgen.randomtool.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.util.ValueUtils;
import org.apache.commons.lang3.RandomUtils;

public class ProtoBufGeneratorTool {

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  public final Object generateObject(final Descriptors.EnumDescriptor descriptor, final String fieldType, final int arraySize, final List<String> fieldValuesList) {
    final List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    Object value = new Object();
    if ("enum".equalsIgnoreCase(fieldType)) {
      value = getEnumOrGenerate(descriptor, fieldType, parameterList);
    } else if ("enum-array".equalsIgnoreCase(fieldType)) {
      value = getArrayEnumOrGenerate(descriptor, fieldType, arraySize, parameterList);
    }
    return value;
  }

  public final Object generateObject(
      final FieldDescriptor descriptor, final String fieldType, final Integer valueLength, final List<String> fieldValuesList,
      final Map<ConstraintTypeEnum, String> constraints) {
    Object result = null;
    if (Objects.nonNull(descriptor.getJavaType())) {
      result = RANDOM_OBJECT.generateRandom(fieldType, valueLength, fieldValuesList, constraints);
    }
    return result;
  }

  private Object getEnumOrGenerate(final Descriptors.EnumDescriptor descriptor, final String fieldType, final List<String> parameterList) {
    Object value = null;

    if ("enum".equalsIgnoreCase(fieldType) || "enum-array".equalsIgnoreCase(fieldType)) {
      if (parameterList.isEmpty()) {
        value = descriptor.getValues().get(RandomUtils.nextInt(0, descriptor.getValues().size()));
      } else {
        final List<Descriptors.EnumValueDescriptor> enumValues = new ArrayList<>(parameterList.size());
        for (String parameter : parameterList) {
          enumValues.add(descriptor.findValueByName(parameter));
        }
        value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
      }
    }
    return value;
  }

  private Object getArrayEnumOrGenerate(final Descriptors.EnumDescriptor descriptor, final String fieldType, final int arraySize, final List<String> parameterList) {
    final List<Object> value = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize; i++) {
      value.add(getEnumOrGenerate(descriptor, fieldType, parameterList));
    }

    return value;
  }
}
