package net.coru.kloadgen.loadgen.impl;

import java.util.List;
import net.coru.kloadgen.input.JsonSchemaProcessor;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.FieldValueMapping;

public class JsonLoadGenerator implements BaseLoadGenerator {

  private JsonSchemaProcessor jsonSchemaProcessor;
  public JsonLoadGenerator(String jsonDocument, List<FieldValueMapping> schemaProperties) {
    jsonSchemaProcessor = new JsonSchemaProcessor(jsonDocument, schemaProperties);
  }

  @Override
  public Object nextMessage() {
    return jsonSchemaProcessor.next();
  }
}
