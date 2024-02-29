/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import org.apache.avro.Conversions;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;

public final class AvroSerializersUtil {

  private AvroSerializersUtil() {
  }

  public static void setupLogicalTypesConversion() {
    final var genericData = GenericData.get();

    genericData.addLogicalTypeConversion(new TimeConversions.DateConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.LocalTimestampMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.LocalTimestampMillisConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimeMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimeMillisConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimestampMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
    genericData.addLogicalTypeConversion(new Conversions.DecimalConversion());
    genericData.addLogicalTypeConversion(new Conversions.UUIDConversion());
  }
}
