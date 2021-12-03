package net.coru.kloadgen.util;

import java.util.HashMap;
import java.util.Map;

public class ProtobufHelper {

    public static final Map<String, String > protobufTypes = new HashMap<>()
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
        put("string", "String");
        put("bytes", "ByteString");
    }};


}
