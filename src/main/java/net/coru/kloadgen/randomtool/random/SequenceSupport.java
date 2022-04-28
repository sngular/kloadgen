package net.coru.kloadgen.randomtool.random;

import com.google.common.collect.ImmutableMap;
import net.coru.kloadgen.randomtool.util.ValidTypeConstants;
import net.coru.kloadgen.randomtool.util.ValueUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class SequenceSupport {

    private static final Map<String, SequenceType<?>> supportedSequenceTypes = ImmutableMap.<String, SequenceType<?>>builder()
            .put(ValidTypeConstants.INT, SequenceType.of(() -> 1, seqObject -> Integer.parseInt(seqObject.toString()) + 1))
            .put(ValidTypeConstants.DOUBLE, SequenceType.of(() -> 1.0, seqObject -> Double.parseDouble(seqObject.toString()) + 1))
            .put(ValidTypeConstants.LONG, SequenceType.of(() -> 1L, seqObject -> Long.parseLong(seqObject.toString()) + 1))
            .put(ValidTypeConstants.FLOAT, SequenceType.of(() -> 1.0f, seqObject -> Float.parseFloat(seqObject.toString()) + 1))
            .put(ValidTypeConstants.SHORT, SequenceType.of(() -> (short) 1, seqObject -> Integer.parseInt(seqObject.toString()) + 1))
            .put(ValidTypeConstants.BYTES_DECIMAL, SequenceType.of(() -> BigDecimal.ONE, seqObject -> new BigDecimal(seqObject.toString()).add(BigDecimal.ONE)))
            .put(ValidTypeConstants.FIXED_DECIMAL, SequenceType.of(() -> BigDecimal.ONE, seqObject -> new BigDecimal(seqObject.toString()).add(BigDecimal.ONE)))
            .build();

    public static boolean isTypeSupported(String fieldType) {
        return supportedSequenceTypes.containsKey(fieldType);
    }

    public Object getFirstValueOrDefaultForType(List<String> fieldValueList, String fieldType) {
        if (!isTypeSupported(fieldType)) {
            throw new IllegalArgumentException("Field type is not supported for sequences");
        }
        if (!fieldValueList.isEmpty()) {
            return ValueUtils.castValue(fieldValueList.get(0), fieldType);
        }

        return supportedSequenceTypes.get(fieldType).getDefaultForType();
    }

    public Object addOneCasted(Object seqObject, String fieldType) {
        if (!isTypeSupported(fieldType)) {
            throw new IllegalArgumentException("Field type is not supported for sequences");
        }
        return supportedSequenceTypes.get(fieldType).addOneCasted.apply(seqObject);
    }


    private static class SequenceType<T> {

        private final Supplier<T> getDefaultForType;
        private final Function<Object, T> addOneCasted;

        public SequenceType(Supplier<T> getDefaultForType, Function<Object, T> addOneCasted) {
            this.getDefaultForType = getDefaultForType;
            this.addOneCasted = addOneCasted;
        }

        public static <T> SequenceType<T> of(Supplier<T> getDefaultForType, Function<Object, T> addOneCasted) {
            return new SequenceType<T>(getDefaultForType, addOneCasted);
        }

        private Object getDefaultForType() {
            return getDefaultForType.get();
        }
    }
}
