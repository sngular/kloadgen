/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.EnumDefinition;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.github.os72.protobuf.dynamic.MessageDefinition.Builder;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.squareup.wire.schema.internal.parser.EnumElement;
import com.squareup.wire.schema.internal.parser.FieldElement;
import com.squareup.wire.schema.internal.parser.MessageElement;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.TypeElement;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.ProtobufHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class SchemaProcessorUtils {

  private static final String OPTIONAL = "optional";

  protected static FieldValueMapping getSafeGetElement(final ArrayDeque<FieldValueMapping> fieldExpMappingsQueue) {
    return !fieldExpMappingsQueue.isEmpty() ? fieldExpMappingsQueue.element() : null;
  }

  protected static String getTypeFilter(final String fieldName, final String cleanPath) {
    String tmpCleanPath = cleanPath.replaceAll(fieldName, "");
    return tmpCleanPath.substring(0, tmpCleanPath.indexOf(".") > 0 ? tmpCleanPath.indexOf(".") + 1 : tmpCleanPath.length());
  }

  public static String getPathUpToFieldName(final String completeFieldName, final int level) {
    String[] splitPath = completeFieldName.split("\\.");
    return String.join(".", Arrays.copyOfRange(splitPath, 0, level));
  }

  protected static boolean isTypeFilterMap(final String singleTypeFilter) {
    return singleTypeFilter.matches("^\\[[1-9]*:]");
  }

  protected static boolean isTypeFilterArray(final String singleTypeFilter) {
    return singleTypeFilter.matches("^\\[\\d*]");
  }

  protected static Integer calculateSizeFromTypeFilter(final String singleTypeFilter) {
    int arrayLength = RandomUtils.nextInt(1, 10);
    String arrayStringSize = "";
    Pattern pattern = Pattern.compile("\\d*");
    Matcher matcher = pattern.matcher(singleTypeFilter);
    while (matcher.find()) {
      if (StringUtils.isNumeric(matcher.group(0))) {
        arrayStringSize = matcher.group(0);
      }
    }
    if (StringUtils.isNotEmpty(arrayStringSize) && StringUtils.isNumeric(arrayStringSize)) {
      arrayLength = Integer.parseInt(arrayStringSize);
    }
    return arrayLength;
  }

  protected static boolean hasMapOrArrayTypeFilter(final String typeFilter) {
    return typeFilter.matches("\\[.*].*");
  }

  protected static String getFirstComplexType(final String completeTypeFilterChain) {
    String firstElementTypeFilterChain = completeTypeFilterChain;
    if (StringUtils.isNotEmpty(firstElementTypeFilterChain)) {
      String[] splitElements = firstElementTypeFilterChain.split("\\.");
      if (splitElements.length > 0) {
        firstElementTypeFilterChain = splitElements[0] + ".";
      }
    }
    Pattern pattern = Pattern.compile("\\[.*?]");
    Matcher matcher = pattern.matcher(firstElementTypeFilterChain);
    return matcher.find() ? matcher.group() : firstElementTypeFilterChain;
  }

  protected static boolean isTypeFilterRecord(final String singleTypeFilter) {
    return singleTypeFilter.startsWith(".");
  }

  protected static boolean isLastTypeFilterOfLastElement(final String completeTypeFilterChain) {
    return !completeTypeFilterChain.matches("\\[.*].*") && !completeTypeFilterChain.matches("\\.");
  }

  protected static boolean isNewFieldSharingRootFieldName(final int level, final FieldValueMapping fieldValueMapping, final String rootFieldName) {
    return getCleanMethodName(fieldValueMapping, level).equalsIgnoreCase(rootFieldName);
  }

  protected static String getCleanMethodName(final FieldValueMapping fieldValueMapping, final int level) {
    return getFullMethodName(fieldValueMapping, level).replaceAll("\\[[0-9]*:?]", "");
  }

  protected static String getFullMethodName(final FieldValueMapping fieldValueMapping, final int level) {
    String pathToClean = cleanUpPath(fieldValueMapping, level);
    int endOfField = pathToClean.contains(".") ? pathToClean.indexOf(".") : pathToClean.length();
    return pathToClean.substring(0, endOfField);
  }

  protected static String cleanUpPath(final FieldValueMapping fieldValueMapping, final int level) {
    String[] splitPath = fieldValueMapping.getFieldName().split("\\.");
    return String.join(".", Arrays.copyOfRange(splitPath, level, splitPath.length));
  }

  public static String[] splitAndNormalizeFullFieldName(final String fullFieldName) {
    String[] fields = fullFieldName.split("\\.");
    List<String> alFields = Arrays.stream(fields).map(field -> field = field.replaceAll("\\[.*]", "")).collect(Collectors.toList());
    return alFields.toArray(new String[0]);
  }

  public static Descriptors.Descriptor buildProtoDescriptor(ProtoFileElement schema) throws Descriptors.DescriptorValidationException, IOException {

    DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
    List<String> imports = schema.getImports();
    for (String importedClass : imports) {
      String schemaToString = new String(SchemaProcessorUtils.class.getClassLoader().getResourceAsStream(importedClass).readAllBytes());
      var lines = new ArrayList<>(CollectionUtils.select(Arrays.asList(schemaToString.split("\\n")), isValid()));
      if (!ProtobufHelper.NOT_ACCEPTED_IMPORTS.contains(importedClass)) {
        var importedSchema = processImported(lines);
        schemaBuilder.addDependency(importedSchema.getFileDescriptorSet().getFile(0).getName());
        schemaBuilder.addSchema(importedSchema);
      }
    }
    MessageElement messageElement = (MessageElement) schema.getTypes().get(0);
    HashMap<String, TypeElement> nestedTypes = new HashMap<>();
    schemaBuilder.setPackage(schema.getPackageName());
    schemaBuilder.addMessageDefinition(buildProtoMessageDefinition(messageElement.getName(), messageElement, nestedTypes));
    return schemaBuilder.build().getMessageDescriptor(messageElement.getName());
  }

  private static MessageDefinition buildProtoMessageDefinition(String fieldName, TypeElement messageElement, HashMap<String, TypeElement> nestedTypes) {
    fillNestedTypes(messageElement, nestedTypes);
    MessageDefinition.Builder msgDef = MessageDefinition.newBuilder(fieldName);
    var element = (MessageElement) messageElement;
    extracted(nestedTypes, msgDef, element.getFields());
    for (var optionalField : element.getOneOfs()) {
      extracted(nestedTypes, msgDef, optionalField.getFields());
    }
    return msgDef.build();
  }

  private static void extracted(final HashMap<String, TypeElement> nestedTypes, final Builder msgDef, final List<FieldElement> fieldElementList) {
    for (var elementField : fieldElementList) {
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
        msgDef.addField("repeated", "typemapnumber" + elementField.getName(), elementField.getName(), elementField.getTag());

        msgDef.addMessageDefinition(
            MessageDefinition.newBuilder("typemapnumber" + elementField.getName()).addField(OPTIONAL, "string", "key", 1).addField(OPTIONAL, realType, "value", 2).build());
      } else if (Objects.nonNull(elementField.getLabel())) {
        msgDef.addField(elementField.getLabel().toString().toLowerCase(), elementField.getType(), elementField.getName(), elementField.getTag());
      } else {
        msgDef.addField(OPTIONAL, elementField.getType(), elementField.getName(), elementField.getTag());
      }
    }
  }

  private static void addDefinition(MessageDefinition.Builder msgDef, String typeName, TypeElement typeElement, HashMap<String, TypeElement> nestedTypes) {
    if (typeElement instanceof EnumElement) {
      var enumElement = (EnumElement) typeElement;
      EnumDefinition.Builder builder = EnumDefinition.newBuilder(enumElement.getName());
      for (var constant : enumElement.getConstants()) {
        builder.addValue(constant.getName(), constant.getTag());
      }
      msgDef.addEnumDefinition(builder.build());
    } else {
      if (!typeName.contains(".")) {
        msgDef.addMessageDefinition(buildProtoMessageDefinition(typeName, typeElement, nestedTypes));
      }
    }
  }

  private static void fillNestedTypes(TypeElement messageElement, HashMap<String, TypeElement> nestedTypes) {
    if (!CollectionUtils.isEmpty(messageElement.getNestedTypes())) {
      messageElement.getNestedTypes().forEach(nestedType -> nestedTypes.put(nestedType.getName(), nestedType));
    }
  }

  private static Predicate<String> isValid() {
    return line -> !line.contains("//") && !line.isEmpty();
  }

  private static DynamicSchema processImported(List<String> importedLines) throws DescriptorValidationException {

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

  private static MessageDefinition buildMessage(final String messageName, final ListIterator<String> messageLines) {

    boolean exit = false;
    MessageDefinition.Builder messageDefinition = MessageDefinition.newBuilder(messageName);
    while (messageLines.hasNext() && !exit) {
      var field = messageLines.next().trim().split("\\s");
      if (ProtobufHelper.isValidType(field[0])) {
        messageDefinition.addField(OPTIONAL, field[0], field[1], Integer.parseInt(checkIfChoppable(field[3])));
      } else if (ProtobufHelper.LABEL.contains(field[0])) {
        messageDefinition.addField(field[0], field[1], field[2], Integer.parseInt(checkIfChoppable(field[4])));
      } else if ("}".equalsIgnoreCase(field[0])) {
        exit = true;
      }
    }

    return messageDefinition.build();
  }

  private static String checkIfChoppable(final String field) {
    String choppedField = field;
    if (field.endsWith(";")) {
      choppedField = StringUtils.chop(field);
    }
    return choppedField;
  }

  private static String checkDotType(final String subfieldType) {
    String dotType = "";
    if (subfieldType.startsWith(".") || subfieldType.contains(".")) {
      String[] typeSplit = subfieldType.split("\\.");
      dotType = typeSplit[typeSplit.length - 1];
    }
    return dotType;
  }

  public static String getOneDimensionValueType( String completeValueType) {
    final int numberOfHyphens = StringUtils.countMatches(completeValueType, "-");
    String [] types = completeValueType.split("-");
    return numberOfHyphens > 1 ? String.join("-", Arrays.copyOfRange(types, 0, 2)): completeValueType;
  }

  public static boolean checkIfOptionalCollection(final FieldValueMapping field, final String fieldName) {
    return !field.getRequired() && field.getFieldValuesList().contains("null") && hasMapOrArrayTypeFilter(fieldName);
  }

  protected static boolean hasTypeFilterInnerRecord(final String completeTypeFilterChain) {
    return completeTypeFilterChain.matches("\\[.*]\\.");
  }

}
