/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.random;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.coru.kloadgen.randomtool.util.ValidTypeConstants;
import net.coru.kloadgen.randomtool.util.ValueUtils;

public class RandomIterator {

    private static final Map<String, SequenceType<?>> supportedIteratorTypes = ImmutableMap.<String, SequenceType<?>>builder()
                                                                                           .put(ValidTypeConstants.INT, SequenceType.of(() -> 1, itObject -> Integer.parseInt(itObject.toString()) + 1))
                                                                                           .put(ValidTypeConstants.DOUBLE, SequenceType.of(() -> 1.0, itObject -> Double.parseDouble(itObject.toString()) + 1))
                                                                                           .put(ValidTypeConstants.LONG, SequenceType.of(() -> 1L, itObject -> Long.parseLong(itObject.toString()) + 1))
                                                                                           .put(ValidTypeConstants.FLOAT, SequenceType.of(() -> 1.0f, itObject -> Float.parseFloat(itObject.toString()) + 1))
                                                                                           .put(ValidTypeConstants.SHORT, SequenceType.of(() -> (short) 1, itObject -> Integer.parseInt(itObject.toString()) + 1))
                                                                                           .put(ValidTypeConstants.BYTES_DECIMAL, SequenceType.of(() -> BigDecimal.ONE, itObject -> new BigDecimal(itObject.toString()).add(BigDecimal.ONE)))
                                                                                           .put(ValidTypeConstants.FIXED_DECIMAL, SequenceType.of(() -> BigDecimal.ONE, itObject -> new BigDecimal(itObject.toString()).add(BigDecimal.ONE)))
                                                                                           .build();

    public static boolean isTypeSupported(String fieldType) {
        return supportedIteratorTypes.containsKey(fieldType);
    }

    public Object generateIt(String fieldName, String fieldType, List<String> fieldValueList, Map<String, Object> context) {
        return context.compute(fieldName, (fieldNameMap, itObject) ->
                itObject == null || !fieldValueList.isEmpty()
                        ? getFirstValueOrDefaultForType(fieldValueList, fieldType)
                        : addOneCasted(itObject, fieldType));
    }

    public Object generateIteratorForFieldValueList(String fieldName, String fieldType, List<String> fieldValueList, Map<String, Object> context) {
        Integer index = (Integer) context.compute(fieldName, (fieldNameMap, itObject) -> itObject == null ? 0 : (((Integer) itObject) + 1) % fieldValueList.size());
        return ValueUtils.castValue(fieldValueList.get(index), fieldType);
    }

    public Object getFirstValueOrDefaultForType(List<String> fieldValueList, String fieldType) {
        if (!isTypeSupported(fieldType)) {
            throw new IllegalArgumentException("Field type is not supported for iterators");
        }
        if (!fieldValueList.isEmpty()) {
            return ValueUtils.castValue(fieldValueList.get(0), fieldType);
        }

        return supportedIteratorTypes.get(fieldType).getDefaultForType();
    }

    public Object addOneCasted(Object itObject, String fieldType) {
        if (!isTypeSupported(fieldType)) {
            throw new IllegalArgumentException("Field type is not supported for iterators");
        }
        return supportedIteratorTypes.get(fieldType).addOneCasted(itObject);
    }
}
