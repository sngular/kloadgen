package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import java.util.List;
import java.util.Map;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import org.apache.avro.Schema;

public class AvroSchemaAdapter extends ConfluentParsedSchemaMetadata{

  public static final String TYPE = "AVRO";

  private Schema schemaObj;
  private String canonicalString;
  private Integer version;
  private List<SchemaReference> references;
  private Map<String, String> resolvedReferences;
  private boolean isNew;

  private transient int hashCode = NO_HASHCODE;

  private static final int NO_HASHCODE = Integer.MIN_VALUE;

 private AvroSchemaAdapter(AvroSchema avroSchema){
   super();
   this.canonicalString = avroSchema.canonicalString();
   this.schemaObj =  avroSchema.rawSchema();
   this.version= avroSchema.version();
   this.references= avroSchema.references();
   this.resolvedReferences=avroSchema.resolvedReferences();
   this.isNew= avroSchema.isNew();
 }

 public static AvroSchemaAdapter parse(AvroSchema avroSchema){
   return new AvroSchemaAdapter(avroSchema);
 }

  public Schema rawSchema() {
    return schemaObj;
  }
}
