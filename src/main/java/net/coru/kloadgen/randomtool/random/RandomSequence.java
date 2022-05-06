/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.random;

import com.google.common.collect.ImmutableMap;
import net.coru.kloadgen.randomtool.util.ValidTypeConstants;
import net.coru.kloadgen.randomtool.util.ValueUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class RandomSequence {

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

    public Object generateSeq(String fieldName, String fieldType, List<String> fieldValueList, Map<String, Object> context) {
        return context.compute(fieldName, (fieldNameMap, seqObject) ->
                seqObject == null
                        ? getFirstValueOrDefaultForType(fieldValueList, fieldType)
                        : addOneCasted(seqObject, fieldType));
    }

    public Object generateSequenceForFieldValueList(String fieldName, String fieldType, List<String> fieldValueList, Map<String, Object> context) {
        Integer index = (Integer) context.compute(fieldName, (fieldNameMap, seqObject) -> seqObject == null ? 0 : (((Integer) seqObject) + 1) % fieldValueList.size());
        return ValueUtils.castValue(fieldValueList.get(index), fieldType);
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
        return supportedSequenceTypes.get(fieldType).addOneCasted(seqObject);
    }
}
