package com.sngular.kloadgen.util;

import java.util.Map;
import java.util.Set;

public final class ProtobufHelper {

  public static final Set<String> LABEL = Set.of("required", "optional");

  public static final Set<String> NOT_ACCEPTED_IMPORTS = Set.of("google/protobuf/descriptor.proto", "google/protobuf/compiler/plugin.proto");

  private static final Map<String, String> PROTOBUF_TYPES =
      Map.ofEntries(
          Map.entry("double", "double"),
          Map.entry("float", "float"),
          Map.entry("int32", "int"),
          Map.entry("int64", "long"),
          Map.entry("uint32", "int"),
          Map.entry("uint64", "long"),
          Map.entry("sint32", "int"),
          Map.entry("sint64", "long"),
          Map.entry("fixed32", "int"),
          Map.entry("fixed64", "long"),
          Map.entry("sfixed32", "int"),
          Map.entry("sfixed64", "long"),
          Map.entry("bool", "boolean"),
          Map.entry("string", "string"),
          Map.entry("bytes", "ByteString"));

  private ProtobufHelper() {}

  public static String translateType(final String type) {
    return PROTOBUF_TYPES.getOrDefault(type, null);
  }

  public static boolean isValidType(final String type) {
    return PROTOBUF_TYPES.containsKey(type);
  }
}
