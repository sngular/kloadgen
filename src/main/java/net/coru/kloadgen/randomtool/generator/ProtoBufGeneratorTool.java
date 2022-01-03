package net.coru.kloadgen.randomtool.generator;

import com.google.protobuf.*;
import com.google.protobuf.Enum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.util.ValueUtils;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProtoBufGeneratorTool {

    private final RandomObject randomObject = new RandomObject();

    public Object generateObject(Descriptors.EnumDescriptor descriptor, String fieldType, List<String> fieldValuesList) {
        List<String> parameterList = ValueUtils.replaceValuesContext(fieldValuesList);
        Object value = new Object();
        if ("enum".equals(fieldType)) {
            value = getEnumOrGenerate(descriptor, fieldType, parameterList);
        }else if("enum-array".equals(fieldType)) {
            value = getArrayEnumOrGenerate(descriptor, fieldType, parameterList);
        }
        return value;
    }

    private Object getArrayEnumOrGenerate(Descriptors.EnumDescriptor descriptor, String fieldType, List<String> parameterList) {
        List<Object> value = new ArrayList(3);
        for(int i = 0; i < 3; i++) {
            value.add(getEnumOrGenerate(descriptor, fieldType, parameterList));
        }

        return value;
    }

    private Object getEnumOrGenerate(Descriptors.EnumDescriptor descriptor, String fieldType, List<String> parameterList) {
        Object value = null;

        if ("enum".equalsIgnoreCase(fieldType) || "enum-array".equalsIgnoreCase(fieldType)) {
            if (parameterList.isEmpty()) {
                throw new KLoadGenException("No enum values on " + descriptor.getFullName());
            } else {
                value = descriptor.getValues().get(RandomUtils.nextInt(0, parameterList.size()));
            }
        }
        return value;
    }
}
