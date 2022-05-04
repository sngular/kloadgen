package net.coru.kloadgen.extractor.extractors;

public final class SchemaExtractorUtil {

  public static final String ARRAY_NAME_POSTFIX = "[]";

  public static final String MAP_NAME_POSTFIX = "[:]";

  public static final String ARRAY_TYPE_POSTFIX = "-array";

  public static final String MAP_TYPE_POSTFIX = "-map";

  public static String postFixNameArray(String fieldName) {
    return fieldName + ARRAY_NAME_POSTFIX;
  }

  public static String postFixNameMap(String fieldName) {
    return fieldName + MAP_NAME_POSTFIX;
  }
}
