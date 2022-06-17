/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.randomtool.random;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.coru.kloadgen.randomtool.util.ValidTypeConstants;
import net.coru.kloadgen.randomtool.util.ValueUtils;

public class RandomIterator {

    private static final Map<String, IteratorType<?>> supportedIteratorTypes = ImmutableMap.<String, IteratorType<?>>builder()
                                                                                           .put(ValidTypeConstants.INT, IteratorType.of(() -> "null"))
                                                                                           .put(ValidTypeConstants.DOUBLE, IteratorType.of(() -> "null"))
                                                                                           .put(ValidTypeConstants.LONG, IteratorType.of(() -> "null"))
                                                                                           .put(ValidTypeConstants.FLOAT, IteratorType.of(() -> "null"))
                                                                                           .put(ValidTypeConstants.SHORT, IteratorType.of(() -> "null"))
                                                                                           .put(ValidTypeConstants.BYTES_DECIMAL, IteratorType.of(() -> "null"))
                                                                                           .put(ValidTypeConstants.FIXED_DECIMAL, IteratorType.of(() -> "null"))
                                                                                           .build();

    public static boolean isTypeSupported(String fieldType) {
        return supportedIteratorTypes.containsKey(fieldType);
    }

    public Object generateIt(String fieldName, String fieldType, List<String> fieldValueList, Map<String, Object> context) {
        return context.compute(fieldName, (fieldNameMap, itObject) ->
                itObject == null || !fieldValueList.isEmpty()
                        ? getValueForType(fieldValueList, fieldType)
                        : getValueFromContext(itObject, fieldType));
    }

    public Object generateIteratorForFieldValueList(String fieldName, String fieldType, List<String> fieldValueList, Map<String, Object> context) {
        Integer index = (Integer) context.compute(fieldName, (fieldNameMap, itObject) -> itObject == null ? 0 : (((Integer) itObject) + 1) % fieldValueList.size());
        return ValueUtils.castValue(fieldValueList.get(index), fieldType);
    }

    public Object getValueForType(List<String> fieldValueList, String fieldType) {
        if (!isTypeSupported(fieldType)) {
            throw new IllegalArgumentException("Field type is not supported for iterators");
        }
        if (!fieldValueList.isEmpty()) {
            return ValueUtils.castValue(fieldValueList.get(0), fieldType);
        }
        return supportedIteratorTypes.get(fieldType).getDefaultForType();
    }

    public Object getValueFromContext(Object itObject, String fieldType) {
        if (!isTypeSupported(fieldType)) {
            throw new IllegalArgumentException("Field type is not supported for iterators");
        }
        return itObject;
    }
}
