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

public final class RandomSequence {

  private static final Map<String, SequenceType<?>> SUPPORTED_SEQUENCE_TYPES =
      ImmutableMap.<String, SequenceType<?>>builder()
                  .put(ValidTypeConstants.INT, SequenceType.of(() -> 1, seqObject -> Integer.parseInt(seqObject.toString()) + 1))
                  .put(ValidTypeConstants.DOUBLE, SequenceType.of(() -> 1.0, seqObject -> Double.parseDouble(seqObject.toString()) + 1))
                  .put(ValidTypeConstants.LONG, SequenceType.of(() -> 1L, seqObject -> Long.parseLong(seqObject.toString()) + 1))
                  .put(ValidTypeConstants.FLOAT, SequenceType.of(() -> 1.0f, seqObject -> Float.parseFloat(seqObject.toString()) + 1))
                  .put(ValidTypeConstants.SHORT, SequenceType.of(() -> (short) 1, seqObject -> Integer.parseInt(seqObject.toString()) + 1))
                  .put(ValidTypeConstants.BYTES_DECIMAL, SequenceType.of(() -> BigDecimal.ONE, seqObject -> new BigDecimal(seqObject.toString()).add(BigDecimal.ONE)))
                  .put(ValidTypeConstants.FIXED_DECIMAL, SequenceType.of(() -> BigDecimal.ONE, seqObject -> new BigDecimal(seqObject.toString()).add(BigDecimal.ONE)))
                  .build();

  private RandomSequence() {
  }

  public static boolean isTypeNotSupported(final String fieldType) {
    return !SUPPORTED_SEQUENCE_TYPES.containsKey(fieldType);
  }

  public static Object generateSeq(final String fieldName, final String fieldType, final List<String> fieldValueList, final Map<String, Object> context) {
    return context.compute(fieldName, (fieldNameMap, seqObject) ->
        seqObject == null
            ? getFirstValueOrDefaultForType(fieldValueList, fieldType)
            : addOneCasted(seqObject, fieldType));
  }

  public static Object generateSequenceForFieldValueList(final String fieldName, final String fieldType, final List<String> fieldValueList, final Map<String, Object> context) {
    final var index = (Integer) context.compute(fieldName, (fieldNameMap, seqObject) -> seqObject == null ? 0 : (((Integer) seqObject) + 1) % fieldValueList.size());
    return ValueUtils.castValue(fieldValueList.get(index), fieldType);
  }

  private static Object getFirstValueOrDefaultForType(final List<String> fieldValueList, final String fieldType) {
    Object result = null;
    if (isTypeNotSupported(fieldType)) {
      throw new IllegalArgumentException("Field type is not supported for sequences");
    }
    if (!fieldValueList.isEmpty()) {
      result = ValueUtils.castValue(fieldValueList.get(0), fieldType);
    } else {
      result = SUPPORTED_SEQUENCE_TYPES.get(fieldType).getDefaultForType();
    }

    return result;
  }

  private static Object addOneCasted(final Object seqObject, final String fieldType) {
    if (isTypeNotSupported(fieldType)) {
      throw new IllegalArgumentException("Field type is not supported for sequences");
    }
    return SUPPORTED_SEQUENCE_TYPES.get(fieldType).addOneCasted(seqObject);
  }
}
