package net.coru.kloadgen.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProtobufHelper {

  private static final Map<String, String> PROTOBUF_TYPES = new HashMap<>();

  public ProtobufHelper() {
    PROTOBUF_TYPES.put("double" , "double");
    PROTOBUF_TYPES.put("float" , "float");
    PROTOBUF_TYPES.put("int32" , "int");
    PROTOBUF_TYPES.put("int64" , "long");
    PROTOBUF_TYPES.put("uint32" , "int");
    PROTOBUF_TYPES.put("uint64" , "long");
    PROTOBUF_TYPES.put("sint32" , "int");
    PROTOBUF_TYPES.put("sint64" , "long");
    PROTOBUF_TYPES.put("fixed32" , "int");
    PROTOBUF_TYPES.put("fixed64" , "long");
    PROTOBUF_TYPES.put("sfixed32" , "int");
    PROTOBUF_TYPES.put("sfixed64" , "long");
    PROTOBUF_TYPES.put("bool" , "boolean");
    PROTOBUF_TYPES.put("string" , "string");
    PROTOBUF_TYPES.put("bytes" , "ByteString");
  }

  public String translateType(String type) {
    return PROTOBUF_TYPES.getOrDefault(type , null);
  }

  public static final Set<String> NOT_ACCEPTED_IMPORTS = Set.of("google/protobuf/descriptor.proto" , "google/protobuf/compiler/plugin.proto");

  public static final Set<String> LABEL = Set.of("required" , "optional");

  public boolean isValidType(String type) {
    return PROTOBUF_TYPES.containsKey(type);
  }
}
