package net.coru.kloadgen.input;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.Iterator;
import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.RandomTool;

public class JsonSchemaProcessor implements Iterator {

  private String jsonDocument;
  private List<FieldValueMapping> schemaProperties;

  public JsonSchemaProcessor(String jsonDocument, List<FieldValueMapping> schemaProperties) {
    this.jsonDocument = jsonDocument;
    this.schemaProperties = schemaProperties;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public Object next() {
    DocumentContext json = JsonPath.parse(jsonDocument);
    for (FieldValueMapping fieldValueMapping: schemaProperties) {
      json.set("$." + fieldValueMapping.getFieldName(),  RandomTool.generateRandom(fieldValueMapping.getValueExpression(),
          fieldValueMapping.getValueLength(), fieldValueMapping.getFieldValuesList()));
    }
    return json.jsonString();
  }
}
