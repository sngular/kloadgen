package net.coru.kloadgen.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProtobufHelper {

    public static final Map<String, String > PROTOBUF_TYPES = new HashMap<>()
    {{
        put("double", "double");
        put("float", "float");
        put("int32", "int");
        put("int64", "long");
        put("uint32", "int");
        put("uint64", "long");
        put("sint32", "int");
        put("sint64", "long");
        put("fixed32", "int");
        put("fixed64", "long");
        put("sfixed32", "int");
        put("sfixed64", "long");
        put("bool", "boolean");
        put("string", "string");
        put("bytes", "ByteString");
    }};

    public static final Set<String> NOT_ACCEPTED_IMPORTS = Set.of("google/protobuf/descriptor.proto", "google/protobuf/compiler/plugin.proto");
    public static final Set<String> LABEL = Set.of("required", "optional");
}
