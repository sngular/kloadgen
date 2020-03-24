package net.coru.kloadgen.loadgen.impl;

import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.input.AvroSchemaProcessor;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import org.apache.jmeter.threads.JMeterContextService;

import java.util.List;

import static net.coru.kloadgen.util.SchemaRegistryKeys.*;

@Slf4j
public class AvroLoadGenerator implements BaseLoadGenerator {

  private AvroSchemaProcessor avroSchemaProcessor;

  public AvroLoadGenerator(String avroSchemaName, List<FieldValueMapping> fieldExprMappings) throws
      KLoadGenException {
    try {

      String schemaRegistryUrl = JMeterContextService.getContext().getProperties().getProperty(SCHEMA_REGISTRY_URL);
      String username = JMeterContextService.getContext().getProperties().getProperty(SCHEMA_REGISTRY_USERNAME_KEY);
      String password = JMeterContextService.getContext().getProperties().getProperty(SCHEMA_REGISTRY_PASSWORD_KEY);

      this.avroSchemaProcessor = new AvroSchemaProcessor(schemaRegistryUrl, avroSchemaName, fieldExprMappings, username, password);
    } catch (Exception exc){
      log.error("Please make sure that properties data type and expression function return type are compatible with each other", exc);
      throw new KLoadGenException(exc);
    }
  }

  @Override
  public Object nextMessage() {
    return avroSchemaProcessor.next();
  }
}
