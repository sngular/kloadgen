package net.coru.kloadgen.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.EnumDefinition;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.squareup.wire.schema.internal.parser.EnumElement;
import com.squareup.wire.schema.internal.parser.MessageElement;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.TypeElement;
import net.coru.kloadgen.util.ProtobufHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;

public class ProtoBufProcessorHelper {

  private static final String OPTIONAL = "optional";

  private final ProtobufHelper protobufHelper = new ProtobufHelper();

  Descriptors.Descriptor buildDescriptor(ProtoFileElement schema) throws Descriptors.DescriptorValidationException, IOException {

    DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
    var lines = new ArrayList<String>();
    List<String> imports = schema.getImports();
    for (String importedClass : imports) {
      String schemaToString = new String(getClass().getClassLoader().getResourceAsStream(importedClass).readAllBytes());
      lines.addAll(CollectionUtils.select(Arrays.asList(schemaToString.split("\\n")), isValid()));
      if (!ProtobufHelper.NOT_ACCEPTED_IMPORTS.contains(importedClass)) {
        var importedSchema = processImported(lines);
        schemaBuilder.addDependency(importedSchema.getFileDescriptorSet().getFile(0).getName());
        schemaBuilder.addSchema(importedSchema);
      }
    }
    MessageElement messageElement = (MessageElement) schema.getTypes().get(0);
    HashMap<String, TypeElement> nestedTypes = new HashMap<>();
    schemaBuilder.setPackage(schema.getPackageName());
    schemaBuilder.addMessageDefinition(buildMessageDefinition(messageElement.getName(), messageElement, nestedTypes));
    return schemaBuilder.build().getMessageDescriptor(messageElement.getName());
  }

  private MessageDefinition buildMessageDefinition(String fieldName, TypeElement messageElement, HashMap<String, TypeElement> nestedTypes) {
    fillNestedTypes(messageElement, nestedTypes);
    MessageDefinition.Builder msgDef = MessageDefinition.newBuilder(fieldName);
    var element = (MessageElement) messageElement;
    for (var elementField : element.getFields()) {
      var elementFieldType = elementField.getType();
      var dotType = checkDotType(elementFieldType);
      if (nestedTypes.containsKey(elementFieldType)) {
        addDefinition(msgDef, elementFieldType, nestedTypes.remove(elementFieldType), nestedTypes);
      }

      if (nestedTypes.containsKey(dotType)) {
        addDefinition(msgDef, dotType, nestedTypes.remove(dotType), nestedTypes);
      }

      if (elementField.getType().startsWith("map")) {
        var realType = StringUtils.chop(elementFieldType.substring(elementFieldType.indexOf(',') + 1).trim());
        var mapDotType = checkDotType(realType);

        if (nestedTypes.containsKey(realType)) {
          addDefinition(msgDef, realType, nestedTypes.remove(realType), nestedTypes);
        }

        if (nestedTypes.containsKey(mapDotType)) {
          addDefinition(msgDef, mapDotType, nestedTypes.remove(mapDotType), nestedTypes);
        }
        msgDef.addField("repeated",
                        "typemapnumber" + elementField.getName(),
                        elementField.getName(),
                        elementField.getTag());

        msgDef.addMessageDefinition(
            MessageDefinition.newBuilder("typemapnumber" + elementField.getName())
                             .addField(OPTIONAL, "string", "key", 1)
                             .addField(OPTIONAL, realType, "value", 2).build()
        );
      } else if (Objects.nonNull(elementField.getLabel())) {
        msgDef.addField(elementField.getLabel().toString().toLowerCase(),
                        elementField.getType(),
                        elementField.getName(),
                        elementField.getTag());
      } else {
        msgDef.addField(OPTIONAL,
                        elementField.getType(),
                        elementField.getName(),
                        elementField.getTag());
      }
    }
    return msgDef.build();
  }

  private void addDefinition(MessageDefinition.Builder msgDef, String typeName, TypeElement typeElement, HashMap<String, TypeElement> nestedTypes) {
    if (typeElement instanceof EnumElement) {
      var enumElement = (EnumElement) typeElement;
      EnumDefinition.Builder builder = EnumDefinition.newBuilder(enumElement.getName());
      for (var constant : enumElement.getConstants()) {
        builder.addValue(constant.getName(), constant.getTag());
      }
      msgDef.addEnumDefinition(builder.build());
    } else {
      if (!typeName.contains(".")) {
        msgDef.addMessageDefinition(buildMessageDefinition(typeName, typeElement, nestedTypes));
      }
    }
  }

  private void fillNestedTypes(TypeElement messageElement, HashMap<String, TypeElement> nestedTypes) {
    if (!CollectionUtils.isEmpty(messageElement.getNestedTypes())) {
      messageElement.getNestedTypes().forEach(nestedType ->
                                                  nestedTypes.put(nestedType.getName(), nestedType)
      );
    }
  }

  private Predicate<String> isValid() {
    return line -> !line.contains("//") && !line.isEmpty();
  }

  private DynamicSchema processImported(List<String> importedLines) throws DescriptorValidationException {

    DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();

    String packageName = "";
    var linesIterator = importedLines.listIterator();
    while (linesIterator.hasNext()) {
      var fileLine = linesIterator.next();

      if (fileLine.startsWith("package")) {
        packageName = StringUtils.chop(fileLine.substring(7).trim());
        schemaBuilder.setPackage(packageName);
      }
      if (fileLine.startsWith("message")) {
        var messageName = StringUtils.chop(fileLine.substring(7).trim()).trim();
        schemaBuilder.setName(packageName + "." + messageName);
        schemaBuilder.addMessageDefinition(buildMessage(messageName, linesIterator));

      }
      if (fileLine.startsWith("import")) {
        schemaBuilder.addDependency(fileLine.substring(6));
      }
    }

    return schemaBuilder.build();
  }

  private MessageDefinition buildMessage(String messageName, ListIterator<String> messageLines) {

    MessageDefinition.Builder messageDefinition = MessageDefinition.newBuilder(messageName);
    while (messageLines.hasNext()) {
      var field = messageLines.next().trim().split("\\s");
      if (protobufHelper.isValidType(field[0])) {
        messageDefinition.addField(OPTIONAL, field[0], field[1], Integer.parseInt(checkIfChoppable(field[3])));
      } else if (ProtobufHelper.LABEL.contains(field[0])) {
        messageDefinition.addField(field[0], field[1], field[2], Integer.parseInt(checkIfChoppable(field[4])));
      }
    }

    return messageDefinition.build();
  }

  public String checkIfChoppable(String field) {
    String choppedField = field;
    if (field.endsWith(";")) {
      choppedField = StringUtils.chop(field);
    }
    return choppedField;
  }

  private String checkDotType(String subfieldType) {
    String dotType = "";
    if (subfieldType.startsWith(".") || subfieldType.contains(".")) {
      String[] typeSplit = subfieldType.split("\\.");
      dotType = typeSplit[typeSplit.length - 1];
    }
    return dotType;
  }
}