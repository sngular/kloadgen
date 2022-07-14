/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.loadgen.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.SchemaProcessor;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class JsonLoadGenerator extends AbstractLoadGenerator implements BaseLoadGenerator {

  private final SchemaProcessor jsonSchemaProcessor;

  private Pair<SchemaMetadata, ParsedSchema> metadata;

  public JsonLoadGenerator() {
    jsonSchemaProcessor = new SchemaProcessor();
  }

  public void setUpGenerator(Map<String, String> originals, String avroSchemaName, List<FieldValueMapping> fieldExprMappings) {
    try {
      metadata = retrieveSchema(originals, avroSchemaName);
      this.jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  public void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings) {
    try {
      var parsedSchema = new JsonSchemaProvider().parseSchema(schema, Collections.emptyList(), true);
      metadata = parsedSchema.map(parsSchema -> Pair.of(new SchemaMetadata(1, 1, "JSON", Collections.emptyList(), schema), parsSchema)).orElse(null);
      this.jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  public EnrichedRecord nextMessage() {
    return EnrichedRecord.builder().schemaMetadata(metadata.getLeft()).genericRecord(jsonSchemaProcessor.next()).build();
  }
}
