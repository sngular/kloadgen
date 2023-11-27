package com.sngular.kloadgen.parsedschema;

public class JsonParsedSchema extends AbstractParsedSchema<String> {

  public JsonParsedSchema(final String name, final String schema) {
    super(name, "JSON", schema);
  }

  @Override
  public final String getRawSchema() {
    return getSchema();
  }
}
