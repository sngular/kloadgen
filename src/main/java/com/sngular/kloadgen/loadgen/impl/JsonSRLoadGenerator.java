/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.loadgen.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.loadgen.BaseLoadGenerator;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.processor.SchemaProcessor;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ConfluentSchemaMetadata;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class JsonSRLoadGenerator implements SRLoadGenerator, BaseLoadGenerator {

  private final SchemaProcessor jsonSchemaProcessor;

  private Pair<BaseSchemaMetadata, ?> metadata;

  public JsonSRLoadGenerator() {
    jsonSchemaProcessor = new SchemaProcessor();
  }

  public void setUpGenerator(final Map<String, String> originals, final String avroSchemaName, final List<FieldValueMapping> fieldExprMappings) {
    try {
      metadata = retrieveSchema(originals, avroSchemaName);
      this.jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, fieldExprMappings);
    } catch (IOException | RestClientException exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  public void setUpGenerator(final String schema, final List<FieldValueMapping> fieldExprMappings) {
    final var parsedSchema = new JsonSchemaProvider().parseSchema(schema, Collections.emptyList(), true);
    metadata =
        parsedSchema.map(parsSchema -> Pair.of(new BaseSchemaMetadata(ConfluentSchemaMetadata.parse(new SchemaMetadata(1, 1, SchemaTypeEnum.JSON.name(), Collections.emptyList(),
                                                                                                                       schema))),
                                               parsSchema)).orElse(null);
    this.jsonSchemaProcessor.processSchema(SchemaTypeEnum.JSON, null, null, fieldExprMappings);
  }

  public EnrichedRecord nextMessage() {
    return EnrichedRecord.builder().schemaMetadata(
        metadata.getLeft().getSchemaMetadataAdapter()
    ).genericRecord(jsonSchemaProcessor.next()).build();
  }

}
