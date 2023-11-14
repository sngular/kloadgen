package com.sngular.kloadgen.parsedschema;

public interface IParsedSchema<T> {

  String schemaType();

  String name();

  String canonicalString();

  Object rawSchema();

}
