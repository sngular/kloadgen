package com.sngular.kloadgen.randomtool.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.randomtool.random.RandomArray;
import com.sngular.kloadgen.randomtool.random.RandomObject;
import com.sngular.kloadgen.randomtool.random.RandomSequence;
import com.sngular.kloadgen.randomtool.util.ValueUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.jmeter.threads.JMeterContextService;

public class ProtoBufGeneratorTool {

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  private static final RandomArray RANDOM_ARRAY = new RandomArray();

  private static final String ENUM_TYPE = "enum";

  private static final String ENUM_ARRAY_TYPE = "enum-array";

  private final Map<String, Object> context = new HashMap<>();

  public final List<Object> generateArray(final String fieldName, final String fieldType, final int arraySize, final Integer valueLength, final List<String> fieldValuesList) {

    final List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(
        fieldValue -> fieldValue.matches("\\$\\{\\w*}") ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) : fieldValue);

    final List<Object> value = new ArrayList<>(arraySize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value.add(RandomSequence.generateSeq(fieldName, fieldType, parameterList, context));
      } else {
        for (int i = arraySize; i > 0; i--) {
          value.add(RandomSequence.generateSeq(fieldName, fieldType, parameterList, context));
        }
      }
    } else {
      value.addAll((List) RANDOM_ARRAY.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap()));
    }

    return value;
  }

  public final Object generateObject(final Descriptors.EnumDescriptor descriptor, final String fieldType, final int arraySize, final List<String> fieldValuesList) {
    final List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    Object value = new Object();
    if (ENUM_TYPE.equalsIgnoreCase(fieldType)) {
      value = getEnumOrGenerate(descriptor, fieldType, parameterList);
    } else if (ENUM_ARRAY_TYPE.equalsIgnoreCase(fieldType)) {
      value = getArrayEnumOrGenerate(descriptor, fieldType, arraySize, parameterList);
    }
    return value;
  }

  public final Object generateObject(
      final FieldDescriptor descriptor, final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Map<ConstraintTypeEnum, String> constraints) {
    Object result = null;
    if (Objects.nonNull(descriptor.getJavaType())) {
      result = generateRawObject(fieldType, valueLength, fieldValuesList, constraints);
    }
    return result;
  }

  public final Object generateRawObject(
      final String fieldType, final Integer valueLength, final List<String> fieldValuesList, final Map<ConstraintTypeEnum, String> constraints) {
    return RANDOM_OBJECT.generateRandom(fieldType, valueLength, fieldValuesList, constraints);
  }

  private Object getEnumOrGenerate(final Descriptors.EnumDescriptor descriptor, final String fieldType, final List<String> parameterList) {
    Object value = null;

    if (ENUM_TYPE.equalsIgnoreCase(fieldType) || ENUM_ARRAY_TYPE.equalsIgnoreCase(fieldType)) {
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
