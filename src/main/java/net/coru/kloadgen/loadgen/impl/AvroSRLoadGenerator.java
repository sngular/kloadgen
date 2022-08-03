/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.loadgen.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.SchemaProcessor;
import net.coru.kloadgen.serializer.EnrichedRecord;
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
    this.avroSchemaProcessor.processSchema(SchemaTypeEnum.AVRO, parser.parse(schema), new SchemaMetadata(1, 1, schema), fieldExprMappings);
  }

  public EnrichedRecord nextMessage() {
    return (EnrichedRecord) avroSchemaProcessor.next();
  }


}
