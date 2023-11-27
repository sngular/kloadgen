package com.sngular.kloadgen.parsedschema;

import org.apache.avro.Schema;

public class AvroParsedSchema extends AbstractParsedSchema<Schema> {

  public AvroParsedSchema(final String name, final Schema schema) {
    super(name, "AVRO", schema);
  }

  @Override
  public final Schema getRawSchema() {
    return getSchema();
  }
}
