/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.loadgen.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;

@Slf4j
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class PlainTextLoadGenerator implements SRLoadGenerator, BaseLoadGenerator {

  private List<FieldValueMapping> fieldExprMappings = new ArrayList<>();

  @Override
  public void setUpGenerator(final Map<String, String> originals, final String avroSchemaName, final List<FieldValueMapping> fieldExprMappings) {
    this.fieldExprMappings = fieldExprMappings;
  }

  @Override
  public void setUpGenerator(final String schema, final List<FieldValueMapping> fieldExprMappings) {
    this.fieldExprMappings = fieldExprMappings;
  }

  public EnrichedRecord nextMessage() {
    final FieldValueMapping fieldValueMapping = fieldExprMappings.get(0);
    return EnrichedRecord.builder().genericRecord(fieldValueMapping.getFieldName()).build();
  }

}
