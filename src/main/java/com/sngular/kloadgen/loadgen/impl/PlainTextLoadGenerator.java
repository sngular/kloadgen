/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.loadgen.impl;

import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.loadgen.BaseLoadGenerator;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.randomtool.util.ValueUtils;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.util.PropsKeysHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.threads.JMeterContextService;

@Slf4j
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class PlainTextLoadGenerator implements SRLoadGenerator, BaseLoadGenerator {

  @Override
  public void setUpGenerator(final Map<String, String> originals, final String avroSchemaName, final List<FieldValueMapping> fieldExprMappings) {
  }

  @Override
  public void setUpGenerator(final String schema, final List<FieldValueMapping> fieldExprMappings) {
  }

  public EnrichedRecord nextMessage() {
    final var value = JMeterContextService.getContext().getVariables().get(PropsKeysHelper.MESSAGE_VALUE);
    return EnrichedRecord.builder().genericRecord(ValueUtils.replaceValueContext(value)).build();
  }

}
