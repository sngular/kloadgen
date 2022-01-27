package net.coru.kloadgen.randomtool.generator;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.EnumValue;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.util.ValueUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProtoBufGeneratorTool {

    private final static RandomObject RANDOM_OBJECT = new RandomObject();;

    private final String ENUM = "enum";
    private final String ENUM_ARRAY = "enum-array";

    public Object generateObject(Descriptors.EnumDescriptor descriptor, String fieldType, int arraySize, List<String> fieldValuesList) {
        List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
        Object value = new Object();
        if (ENUM.equalsIgnoreCase(fieldType)) {
            value = getEnumOrGenerate(descriptor, fieldType, parameterList);
        }else if(ENUM_ARRAY.equalsIgnoreCase(fieldType)) {
            value = getArrayEnumOrGenerate(descriptor, fieldType, arraySize, parameterList);
        }
        return value;
    }

    private Object getArrayEnumOrGenerate(Descriptors.EnumDescriptor descriptor, String fieldType, int arraySize, List<String> parameterList) {
        List<Object> value = new ArrayList<>(arraySize);
        for(int i = 0; i < arraySize; i++) {
            value.add(getEnumOrGenerate(descriptor, fieldType, parameterList));
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
                for(String parameter: parameterList) {
                    enumValues.add(descriptor.findValueByName(parameter));
                }
                value = enumValues.get(RandomUtils.nextInt(0, enumValues.size()));
            }
        }
        return value;
    }

    public Object generateObject(FieldDescriptor descriptor, String fieldType, Integer valueLength, List<String> fieldValuesList,
        Map<ConstraintTypeEnum, String> constrains) {
        Object result = null;
        if(Objects.nonNull(descriptor.getJavaType())) {
            result = RANDOM_OBJECT.generateRandom(fieldType, valueLength, fieldValuesList, constrains);
        }
        return result;
    }
}
