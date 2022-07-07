package net.coru.kloadgen.randomtool.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.random.RandomSequence;
import net.coru.kloadgen.randomtool.util.ValueUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.jmeter.threads.JMeterContextService;

public class ProtoBufGeneratorTool {

  private static final RandomObject RANDOM_OBJECT = new RandomObject();

  private static final RandomArray RANDOM_ARRAY = new RandomArray();

  private static final Map<String, Object> CONTEXT = new HashMap<>();

  public static Object generateArray(final String fieldName, final String fieldType, final int arraySize, final Integer valueLength, final List<String> fieldValuesList) {

    final List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}")
                                     ? JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                                                                                                                                  fieldValue
    );

    final List value = new ArrayList<>(arraySize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || RandomSequence.isTypeNotSupported(fieldType))) {
        value.add(RandomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, CONTEXT));
      } else {
        for (int i = arraySize; i > 0; i--) {
          value.add(RandomSequence.generateSeq(fieldName, fieldType, parameterList, CONTEXT));
        }
      }
    } else {
      value.addAll((ArrayList) RANDOM_ARRAY.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap()));
    }

    return value;
  }

  public Object generateObject(Descriptors.EnumDescriptor descriptor, String fieldType, int arraySize, List<String> fieldValuesList) {
    List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
    Object value = new Object();
    if ("enum".equalsIgnoreCase(fieldType)) {
      value = getEnumOrGenerate(descriptor, fieldType, parameterList);
    } else if ("enum-array".equalsIgnoreCase(fieldType)) {
      value = getArrayEnumOrGenerate(descriptor, fieldType, arraySize, parameterList);
    }
    return value;
  }

  private Object getEnumOrGenerate(Descriptors.EnumDescriptor descriptor, String fieldType, List<String> parameterList) {
    Object value = null;

    if ("enum".equalsIgnoreCase(fieldType) || "enum-array".equalsIgnoreCase(fieldType)) {
      if (parameterList.isEmpty()) {
        value = descriptor.getValues().get(RandomUtils.nextInt(0, descriptor.getValues().size()));
      } else {
        List<Descriptors.EnumValueDescriptor> enumValues = new ArrayList<>(parameterList.size());
        for (String parameter : parameterList) {
          enumValues.add(descriptor.findValueByName(parameter));
        }
        value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
      }
    }
    return value;
  }

  private Object getArrayEnumOrGenerate(Descriptors.EnumDescriptor descriptor, String fieldType, int arraySize, List<String> parameterList) {
    List<Object> value = new ArrayList<>(arraySize);
    for (int i = 0; i < arraySize; i++) {
      value.add(getEnumOrGenerate(descriptor, fieldType, parameterList));
    }

    return value;
  }

  public Object generateObject(
      FieldDescriptor descriptor, String fieldType, Integer valueLength, List<String> fieldValuesList,
      Map<ConstraintTypeEnum, String> constraints) {
    Object result = null;
    if (Objects.nonNull(descriptor.getJavaType())) {
      result = RANDOM_OBJECT.generateRandom(fieldType, valueLength, fieldValuesList, constraints);
    }
    return result;
  }
}
