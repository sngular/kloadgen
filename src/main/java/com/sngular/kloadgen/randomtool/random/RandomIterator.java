/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.random;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.sngular.kloadgen.randomtool.util.ValidTypeConstants;
import com.sngular.kloadgen.randomtool.util.ValueUtils;

public final class RandomIterator {

  private static final Map<String, IteratorType<?>> SUPPORTED_ITERATOR_TYPES = ImmutableMap.<String, IteratorType<?>>builder()
                                                                                           .put(ValidTypeConstants.INT, IteratorType.of(() -> null))
                                                                                           .put(ValidTypeConstants.DOUBLE, IteratorType.of(() -> null))
                                                                                           .put(ValidTypeConstants.LONG, IteratorType.of(() -> null))
                                                                                           .put(ValidTypeConstants.FLOAT, IteratorType.of(() -> null))
                                                                                           .put(ValidTypeConstants.SHORT, IteratorType.of(() -> null))
                                                                                           .put(ValidTypeConstants.BYTES_DECIMAL, IteratorType.of(() -> null))
                                                                                           .put(ValidTypeConstants.FIXED_DECIMAL, IteratorType.of(() -> null)).build();
  
  private RandomIterator() {}

  public static boolean isTypeNotSupported(final String fieldType) {
    return !SUPPORTED_ITERATOR_TYPES.containsKey(fieldType);
  }

  public static Object generateIt(final String fieldName, final String fieldType, final List<String> fieldValueList, final Map<String, Object> context) {
    return context.compute(fieldName, (fieldNameMap, itObject) -> !fieldValueList.isEmpty() ? getValueForType(fieldValueList, fieldType) : null);
  }

  public static Object generateIteratorForFieldValueList(final String fieldName, final String fieldType, final List<String> fieldValueList, final Map<String, Object> context) {
    final var index = (Integer) context.compute(fieldName, (fieldNameMap, itObject) -> itObject == null ? 0 : (((Integer) itObject) + 1) % fieldValueList.size());
    return ValueUtils.castValue(fieldValueList.get(index), fieldType);
  }

  private static Object getValueForType(final List<String> fieldValueList, final String fieldType) {
    if (isTypeNotSupported(fieldType)) {
      throw new IllegalArgumentException("Field type is not supported for iterators");
    }
    return ValueUtils.castValue(fieldValueList.get(0), fieldType);
  }

}