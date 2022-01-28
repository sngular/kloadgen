package net.coru.kloadgen.loadgen.impl;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.ProtobufSchemaProcessor;
import net.coru.kloadgen.serializer.EnrichedRecord;
import org.apache.avro.Schema;

@Slf4j
public class ProtobufLoadGenerator implements BaseLoadGenerator {

  private SchemaRegistryClient schemaRegistryClient;

  private SchemaMetadata metadata;

  private final ProtobufSchemaProcessor protobufSchemaProcessor;

  public ProtobufLoadGenerator() {
    protobufSchemaProcessor = new ProtobufSchemaProcessor();
  }

  @Override
  public void setUpGenerator(Map<String, String> originals , String avroSchemaName , List<FieldValueMapping> fieldExprMappings) {
    try {
      this.protobufSchemaProcessor.processSchema(retrieveSchema(originals , avroSchemaName) , metadata , fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other" , exc);
      throw new KLoadGenException(exc);
    }
  }

  @Override
  public void setUpGenerator(String schema , List<FieldValueMapping> fieldExprMappings) {
    try {
      Schema.Parser parser = new Schema.Parser();
      this.protobufSchemaProcessor.processSchema((ParsedSchema) parser.parse(schema) , new SchemaMetadata(1 , 1 , schema) , fieldExprMappings);
    } catch (Exception exc) {
      log.error("Please make sure that properties data type and expression function return type are compatible with each other" , exc);
      throw new KLoadGenException(exc);
    }
  }

  @Override
  public EnrichedRecord nextMessage() {
    try {
      return protobufSchemaProcessor.next();
    } catch (DescriptorValidationException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private ParsedSchema retrieveSchema(Map<String, String> originals , String avroSchemaName) throws IOException, RestClientException {
    schemaRegistryClient = new CachedSchemaRegistryClient(originals.get(SCHEMA_REGISTRY_URL_CONFIG) , 1000 , originals);
    return getSchemaBySubject(avroSchemaName);
  }

  private ParsedSchema getSchemaBySubject(String avroSubjectName) throws IOException, RestClientException {
    metadata = schemaRegistryClient.getLatestSchemaMetadata(avroSubjectName);
    return schemaRegistryClient.getSchemaBySubjectAndId(avroSubjectName , metadata.getId());
  }
}
