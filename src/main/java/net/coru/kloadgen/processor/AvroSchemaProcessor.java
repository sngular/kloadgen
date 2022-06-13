/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreator;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreatorFactory;
import net.coru.kloadgen.serializer.EnrichedRecord;

public class AvroSchemaProcessor extends SchemaProcessorLib {

  private List<FieldValueMapping> fieldExprMappings;

  private ProcessorObjectCreator objectCreator;

  public void processSchema(Object schema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
    this.objectCreator = ProcessorObjectCreatorFactory.getInstance(SchemaTypeEnum.AVRO, schema, metadata);
    this.fieldExprMappings = fieldExprMappings;
  }

  public EnrichedRecord next() {
    EnrichedRecord enrichedRecord = null;
    //GenericRecord entity = new GenericData.Record(schema);
    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      enrichedRecord = (EnrichedRecord) this.objectCreator.createObjectChain(fieldExpMappingsQueue);
    }
    return enrichedRecord;
  }

}
