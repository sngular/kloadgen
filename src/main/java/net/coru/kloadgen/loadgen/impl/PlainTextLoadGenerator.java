/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.loadgen.impl;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.randomtool.util.ValueUtils;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.util.PropsKeysHelper;
import org.apache.commons.lang3.NotImplementedException;
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
