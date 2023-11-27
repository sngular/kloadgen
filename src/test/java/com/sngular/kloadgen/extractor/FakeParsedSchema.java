package com.sngular.kloadgen.extractor;

import com.sngular.kloadgen.parsedschema.AbstractParsedSchema;

public class FakeParsedSchema extends AbstractParsedSchema<String> {

  public FakeParsedSchema(final String name, final String type, final String schema) {
    super(name, type, schema);
  }

  @Override
  public final String getRawSchema() {
    return getSchema();
  }
}
