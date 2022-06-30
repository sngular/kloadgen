package net.coru.kloadgen.processor.objectcreator.impl;

import static org.apache.avro.Schema.Type.ARRAY;
import static org.apache.avro.Schema.Type.BOOLEAN;
import static org.apache.avro.Schema.Type.BYTES;
import static org.apache.avro.Schema.Type.DOUBLE;
import static org.apache.avro.Schema.Type.FIXED;
import static org.apache.avro.Schema.Type.FLOAT;
import static org.apache.avro.Schema.Type.INT;
import static org.apache.avro.Schema.Type.LONG;
import static org.apache.avro.Schema.Type.MAP;
import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.STRING;
import static org.apache.avro.Schema.Type.UNION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.processor.SchemaProcessorLib;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreator;
import net.coru.kloadgen.randomtool.generator.AvroGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomArray;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;
import net.coru.kloadgen.randomtool.random.RandomSequence;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.jmeter.threads.JMeterContextService;

public class AvroObjectCreator extends SchemaProcessorLib implements ProcessorObjectCreator {

  private final Set<Type> typesSet = EnumSet.of(INT, DOUBLE, FLOAT, BOOLEAN, STRING, LONG, BYTES, FIXED);

  private final Schema schema;

  private final SchemaMetadata metadata;

  private final AvroGeneratorTool avroGeneratorTool;

  private final RandomObject randomObject;

  private final RandomMap randomMap;

  private final RandomArray randomArray;

  private static final RandomSequence randomSequence = new RandomSequence();

  private final Map<String, GenericRecord> entity = new HashMap<>();

  public AvroObjectCreator(Object schema, SchemaMetadata metadata) {
    if(schema instanceof ParsedSchema) {
      this.schema = (Schema) ((ParsedSchema)schema).rawSchema();
    }
    else {
      this.schema = (Schema) schema;
    }
    this.metadata = metadata;
    this.randomObject = new RandomObject();
    this.randomMap = new RandomMap();
    this.randomArray = new RandomArray();
    this.avroGeneratorTool = new AvroGeneratorTool();
  }

  @Override
  public String generateRandomString(final Integer valueLength) {
    return String.valueOf(randomObject.generateRandom("string", valueLength, Collections.emptyList(), Collections.emptyMap()));
  }

  @Override
  public boolean isOptionalField(final Object field, final boolean isRequired, final String nameOfField, final List<String> fieldValuesList) {
    return false;
  }

  @Override
  public Object createBasicArrayMap(final String fieldName, final String fieldType, final Integer arraySize, final Integer valueLength, final List<String> fieldValuesList) {
    return null;
  }

  @Override
  public Object createBasicMapArray(String fieldType, Integer calculateSize, List<String> fieldValuesList, Integer valueLength, Integer arraySize, Map<ConstraintTypeEnum, String> constraints) {
    return null;
  }

  @Override
  public Object createBasicMap(final String fieldName, final String fieldType, final Integer mapSize, final Integer fieldValueLength, final List<String> fieldValuesList) {
    /*List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}") ?
                                     JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                     fieldValue
    );

    var value = new HashMap<>(mapSize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomSequence.isTypeSupported(fieldType))) {
        value.put(generateMapKey(), randomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, context));
      } else {
        for (int i = mapSize; i > 0; i--) {
          value.put(generateMapKey(), randomSequence.generateSeq(fieldName, fieldType, parameterList, context));
        }
      }
    } else {
      return randomMap.generateMap(fieldType, mapSize, parameterList, fieldValueLength, 0, Collections.emptyMap());
    }

    return value;*/
    return null;
  }

  @Override
  public Object createBasicArray(final String fieldName, final String fieldType, final Integer arraySize, final Integer valueLength, final List<String> fieldValuesList) {
    List<String> parameterList = new ArrayList<>(fieldValuesList);
    parameterList.replaceAll(fieldValue ->
                                 fieldValue.matches("\\$\\{\\w*}") ?
                                     JMeterContextService.getContext().getVariables().get(fieldValue.substring(2, fieldValue.length() - 1)) :
                                     fieldValue
    );
    List value = new ArrayList<>(arraySize);
    if ("seq".equals(fieldType)) {
      if (!fieldValuesList.isEmpty() && (fieldValuesList.size() > 1 || !RandomSequence.isTypeSupported(fieldType))) {
        value.add(randomSequence.generateSequenceForFieldValueList(fieldName, fieldType, parameterList, context));
      } else {
        for (int i = arraySize; i > 0; i--) {
          value.add(randomSequence.generateSeq(fieldName, fieldType, parameterList, context));
        }
      }
    } else {
      value = (ArrayList) randomArray.generateArray(fieldType, valueLength, parameterList, arraySize, Collections.emptyMap());
    }

    return value;
  }

  @Override
  public Object createFinalField(final String fieldName, final String fieldType, final Integer valueLength, final List<String> fieldValuesList) {
    Schema innerSchema = this.schema;
    if (this.schema.getType().equals(MAP)) {
      innerSchema = this.schema.getValueType();
    } else if (this.schema.getType().equals(ARRAY)) {
      innerSchema = innerSchema.getElementType();
    }
    GenericRecord subEntity = createRecord(innerSchema);
    if (null == subEntity) {
      throw new KLoadGenException("Something Odd just happened");
    } else {
      subEntity.getSchema();
    }
    /*return avroGeneratorTool.generateObject(
        subEntity.getSchema().getField(fieldName),
        fieldName, fieldType, valueLength, fieldValuesList,
        extractConstraints(subEntity.getSchema().getField(fieldName))
    );*/
    return null;
  }

  private GenericRecord createRecord(Schema schema) {
    if (RECORD == schema.getType()) {
      return new GenericData.Record(schema);
    } else if (UNION == schema.getType()) {
      return createRecord(getRecordUnion(schema.getTypes()));
    } else if (ARRAY == schema.getType()) {
      return createRecord(schema.getElementType());
    } else if (MAP == schema.getType()) {
      return createRecord(schema.getElementType());
    } else {
      return null;
    }
  }

  private Schema getRecordUnion(List<Schema> types) {
    Schema isRecord = null;
    for (Schema innerSchema : types) {
      if (RECORD == innerSchema.getType() || ARRAY == innerSchema.getType() || MAP == innerSchema.getType() || typesSet.contains(innerSchema.getType())) {
        isRecord = innerSchema;
      }
    }
    return isRecord;
  }
}
