/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.loadgen.impl;

import java.util.List;
import java.util.Map;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.AvroSchemaProcessor;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;

@Slf4j
public class AvroLoadGenerator extends AbstractLoadGenerator implements BaseLoadGenerator {

  private final AvroSchemaProcessor avroSchemaProcessor;

  public AvroLoadGenerator() {
    avroSchemaProcessor = new AvroSchemaProcessor();
  }

  public void setUpGenerator(Map<String, String> originals, String avroSchemaName, List<FieldValueMapping> fieldExprMappings) {
    try {
      var schema = retrieveSchema(originals, avroSchemaName);
      this.avroSchemaProcessor.processSchema(schema.getRight(), schema.getLeft(), fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  public void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings) {
    try {
      Schema.Parser parser = new Schema.Parser();
      this.avroSchemaProcessor.processSchema(parser.parse(schema), new SchemaMetadata(1, 1, schema), fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  public EnrichedRecord nextMessage() {
    return avroSchemaProcessor.next();
  }


}
