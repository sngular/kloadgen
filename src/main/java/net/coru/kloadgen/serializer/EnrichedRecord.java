package net.coru.kloadgen.serializer;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.Value;
import org.apache.avro.generic.GenericRecord;

@Value
public class EnrichedRecord {

  SchemaMetadata schemaMetadata;
  GenericRecord genericRecord;
}
