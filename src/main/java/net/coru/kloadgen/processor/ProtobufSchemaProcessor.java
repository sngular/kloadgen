package net.coru.kloadgen.processor;

import java.io.IOException;
import java.util.List;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ObjectCreatorFactory;
import net.coru.kloadgen.serializer.EnrichedRecord;

@Slf4j
public class ProtobufSchemaProcessor extends SchemaProcessorLib {

  public static final String STRING_TYPE = "string";

  private List<FieldValueMapping> fieldExprMappings;

  //private ProcessorObjectCreator objectCreator;


  public EnrichedRecord next() {
    /*DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(schema);

    if (Objects.nonNull(fieldExprMappings) && !fieldExprMappings.isEmpty()) {
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueue = new ArrayDeque<>(fieldExprMappings);
      ArrayDeque<FieldValueMapping> fieldExpMappingsQueueCopy = new ArrayDeque<>(fieldExprMappings);
      fieldExpMappingsQueueCopy.poll();

      FieldValueMapping fieldValueMapping = fieldExpMappingsQueue.element();

      int generatedProperties = 0;
      int elapsedProperties = 0;

      while (!fieldExpMappingsQueue.isEmpty()) {
        String methodName = cleanUpPath(fieldValueMapping, "");
        String fieldName = getCleanMethodName(fieldValueMapping, "");
        String typeFilter = methodName.replaceAll(fieldName, "");
        String fieldType = fieldValueMapping.getFieldType();

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

        if (isOptionalField(messageBuilder, fieldName) && !fieldValueMapping.getRequired() && fieldValueMapping.getFieldValuesList().contains("null")) {
          elapsedProperties++;
          fieldExpMappingsQueue.remove();
        } else {

          if (typeFilter.matches("\\[.*]\\[.*") && !fieldType.endsWith("map-map") && !fieldType.endsWith("array-array")) {
            if (checkIfIsRecordMapArray(methodName)) {
              processFieldValueMappingAsRecordMapArray(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (checkIfIsRecordArrayMap(methodName)) {
              processFieldValueMappingAsRecordArrayMap(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (checkIfArrayMap(Objects.requireNonNull(fieldType))) {
              processFieldValueMappingAsSimpleArrayMap(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (checkIfMapArray(fieldType)) {
              processFieldValueMappingAsSimpleMapArray(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else {
              throw new KLoadGenException("Wrong configuration Map - Array");
            }
          } else if (typeFilter.startsWith("[")) {
            if (checkIfRecordMap(typeFilter)) {
              processFieldValueMappingAsRecordMap(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (checkIfRecordArray(typeFilter)) {
              processFieldValueMappingAsRecordArray(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (checkIfMap(typeFilter, fieldType)) {
              processFieldValueMappingAsSimpleMap(fieldExpMappingsQueue, messageBuilder, fieldName);
            } else if (checkIfArray(typeFilter, fieldType)) {
              processFieldValueMappingAsSimpleArray(fieldExpMappingsQueue, messageBuilder, "", fieldName);
            } else {
              throw new KLoadGenException("Wrong configuration Map - Array");
            }
          } else if (typeFilter.startsWith(".")) {
            String fieldNameSubEntity = getCleanMethodName(fieldValueMapping, "");
            messageBuilder.setField(getFieldDescriptorForField(messageBuilder, fieldName),
                                    createObject(getDescriptorForField(messageBuilder, fieldNameSubEntity), fieldNameSubEntity, fieldExpMappingsQueue));
          } else {
            fieldExpMappingsQueue.poll();
            generateObject(messageBuilder, fieldValueMapping, fieldName);
          }
        }
        fieldValueMapping = getSafeGetElement(fieldExpMappingsQueue);

      }
    }
    return new EnrichedRecord(metadata, messageBuilder.build());*/
    return null;
  }
}
