/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.loadgen.impl;

import java.io.IOException;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;

@Slf4j
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class AvroSRLoadGenerator implements SRLoadGenerator, BaseLoadGenerator {

  private final SchemaProcessor avroSchemaProcessor;

  public AvroSRLoadGenerator() {
    avroSchemaProcessor = new SchemaProcessor();
  }

  public void setUpGenerator(final Map<String, String> originals, final String avroSchemaName, final List<FieldValueMapping> fieldExprMappings) {
    try {
      final var schema = retrieveSchema(originals, avroSchemaName);
      this.avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, schema.getRight(), schema.getLeft(), fieldExprMappings);
    } catch (final IOException | RestClientException exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  public void setUpGenerator(final String schema, final List<FieldValueMapping> fieldExprMappings) {
    final Schema.Parser parser = new Schema.Parser();
    this.avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, parser.parse(schema),
                                           new BaseSchemaMetadata<>(ConfluentSchemaMetadata.parse(new SchemaMetadata(1, 1,
                                                                                                                     schema))),
                                           fieldExprMappings);
  }

  public EnrichedRecord nextMessage() {
    return (EnrichedRecord) avroSchemaProcessor.next();
  }


}
