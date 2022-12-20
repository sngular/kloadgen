package com.sngular.kloadgen.extractor.extractors;

public final class SchemaExtractorUtil {

  public static final String ARRAY_NAME_POSTFIX = "[]";

  public static final String MAP_NAME_POSTFIX = "[:]";

  public static final String ARRAY_TYPE_POSTFIX = "-array";

  public static final String MAP_TYPE_POSTFIX = "-map";

  private SchemaExtractorUtil() {
  }

  public static String postFixNameArray(final String fieldName) {
    return fieldName + ARRAY_NAME_POSTFIX;
  }

  public static String postFixNameMap(final String fieldName) {
    return fieldName + MAP_NAME_POSTFIX;
  }
}
