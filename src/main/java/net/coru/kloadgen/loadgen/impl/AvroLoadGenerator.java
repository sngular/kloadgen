package net.coru.kloadgen.loadgen.impl;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.AvroSchemaProcessor;

@Slf4j
public class AvroLoadGenerator implements BaseLoadGenerator {

  private final AvroSchemaProcessor avroSchemaProcessor;

  public AvroLoadGenerator(String avroSchemaName, List<FieldValueMapping> fieldExprMappings) throws
      KLoadGenException {
    try {
      this.avroSchemaProcessor = new AvroSchemaProcessor(avroSchemaName, fieldExprMappings);
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
