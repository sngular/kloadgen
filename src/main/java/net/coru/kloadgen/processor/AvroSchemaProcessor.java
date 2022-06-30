/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.MAP;
import static org.apache.avro.Schema.Type.NULL;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreator;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreatorFactory;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections4.IteratorUtils;

public class AvroSchemaProcessor extends SchemaProcessorLib {

  private List<FieldValueMapping> fieldExprMappings;

  private ProcessorObjectCreator objectCreator;

  public void processSchema(Object schema, SchemaMetadata metadata, List<FieldValueMapping> fieldExprMappings) {
    this.objectCreator = ProcessorObjectCreatorFactory.getInstance(SchemaTypeEnum.AVRO, schema, metadata);
    this.fieldExprMappings = fieldExprMappings;
  }

  public EnrichedRecord next() {
    /*GenericRecord entity = new GenericData.Record(schema);
    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();
      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      int generatedProperties = 0;
      int elapsedProperties = 0;

      while (!fieldExpMappingsQueue.isEmpty()) {
        String cleanPath = cleanUpPath(fieldValueMapping, "");
        String fieldName = getCleanMethodName(fieldValueMapping, "");
        String typeFilter = cleanPath.replaceAll(fieldName, "");

        if ((fieldExpMappingsQueueCopy.peek() == null || !fieldExpMappingsQueueCopy.peek().getFieldName().contains(fieldName))
            && (generatedProperties == elapsedProperties && generatedProperties > 0) && fieldValueMapping.getAncestorRequired()) {
          fieldValueMapping.setRequired(true);
          List<String> temporalFieldValueList = fieldValueMapping.getFieldValuesList();
          temporalFieldValueList.remove("null");
          fieldValueMapping.setFieldValuesList(temporalFieldValueList.toString());
          fieldExpMappingsQueueCopy.poll();
        } else {
          generatedProperties = 0;
          elapsedProperties = 0;
          fieldExpMappingsQueueCopy.poll();
        }
        generatedProperties++;

        if (isOptionalField(schema.getField(fieldName)) && !fieldValueMapping.getRequired() && fieldValueMapping.getFieldValuesList().contains("null")) {
          elapsedProperties++;
          fieldExpMappingsQueue.remove();
          fieldValueMapping = fieldExpMappingsQueue.peek();
        } else {

          if (typeFilter.matches("\\[?..*]\\[.*") && !fieldValueMapping.getFieldType().endsWith("map-map") && !fieldValueMapping.getFieldType().endsWith("array-array") &&
              !typeFilter.startsWith(".")) {
            if (checkIfArrayMap(Objects.requireNonNull(fieldValueMapping).getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfMapArray(fieldValueMapping.getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfIsRecordMapArray(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfIsRecordArrayMap(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, entity, fieldName);
            } else {
              throw new KLoadGenException("Wrong configuration Map - Array");
            }
          } else if (typeFilter.startsWith("[")) {
            if (checkIfMap(typeFilter, fieldValueMapping.getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfArray(typeFilter, fieldValueMapping.getFieldType())) {
              fieldValueMapping = processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfRecordArray(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, entity, fieldName);
            } else if (checkIfRecordMap(cleanPath)) {
              fieldValueMapping = processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, entity, fieldName);
            } else {
              throw new KLoadGenException("Wrong configuration Map - Array");
            }
          } else if (typeFilter.startsWith(".")) {
            entity.put(fieldName, createObject(entity.getSchema().getField(fieldName).schema(), fieldName, fieldExpMappingsQueue));
            fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);
          } else {
            entity.put(Objects.requireNonNull(fieldValueMapping).getFieldName(),
                       this.objectCreator.createFinalField(fieldValueMapping.getFieldName(), fieldValueMapping.getFieldType(), fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList())
            );
            fieldExpMappingsQueue.remove();
            fieldValueMapping = fieldExpMappingsQueue.peek();
          }
        }
      }
    }
    return new EnrichedRecord(metadata, entity);*/
    return null;
  }

}
