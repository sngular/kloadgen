package com.sngular.kloadgen.parsedschema;

import lombok.Getter;

@Getter
public abstract class AbstractParsedSchema<T> {

  private final T schema;
  private final String schemaType;
  private final String name;

  public AbstractParsedSchema(final String name, final String type, final T schema) {
    this.name = name;
    this.schemaType = type;
    this.schema = schema;
  }

  public final T schema() {
    return this.schema;
  }

  public final String schemaType() {
    return this.schemaType;
  }

  public abstract T getRawSchema();
}
