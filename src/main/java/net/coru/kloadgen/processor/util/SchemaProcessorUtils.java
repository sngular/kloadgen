/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.processor.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.ProtobufHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class SchemaProcessorUtils {

  private static final String OPTIONAL = "optional";

  private SchemaProcessorUtils() {
  }

  public static String getTypeFilter(final String fieldName, final String cleanPath) {
    final String tmpCleanPath = cleanPath.replaceAll(fieldName, "");
    return tmpCleanPath.substring(0, tmpCleanPath.indexOf(".") > 0 ? tmpCleanPath.indexOf(".") + 1 : tmpCleanPath.length());
  }

  public static String getPathUpToFieldName(final String completeFieldName, final int level) {
    final String[] splitPath = completeFieldName.split("\\.");
    return String.join(".", Arrays.copyOfRange(splitPath, 0, level));
  }

  public static boolean isTypeFilterMap(final String singleTypeFilter) {
    return singleTypeFilter.matches("^\\[[1-9]*:]");
  }

  public static boolean isTypeFilterArray(final String singleTypeFilter) {
    return singleTypeFilter.matches("^\\[\\d*]");
  }

  public static Integer calculateSizeFromTypeFilter(final String singleTypeFilter) {
    int arrayLength = RandomUtils.nextInt(1, 10);
    String arrayStringSize = "";
    final Pattern pattern = Pattern.compile("\\d*");
    final Matcher matcher = pattern.matcher(singleTypeFilter);
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

  public static String getFirstComplexType(final String completeTypeFilterChain) {
    String firstElementTypeFilterChain = completeTypeFilterChain;
    if (StringUtils.isNotEmpty(firstElementTypeFilterChain)) {
      final String[] splitElements = firstElementTypeFilterChain.split("\\.");
      if (splitElements.length > 0) {
        firstElementTypeFilterChain = splitElements[0] + ".";
      }
    }
    final Pattern pattern = Pattern.compile("\\[.*?]");
    final Matcher matcher = pattern.matcher(firstElementTypeFilterChain);
    return matcher.find() ? matcher.group() : firstElementTypeFilterChain;
  }

  public static boolean isTypeFilterRecord(final String singleTypeFilter) {
    return singleTypeFilter.startsWith(".");
  }

  public static boolean isLastTypeFilterOfLastElement(final String completeTypeFilterChain) {
    return !completeTypeFilterChain.matches("\\[.*].*") && !completeTypeFilterChain.matches("\\.");
  }

  public static boolean isNewFieldSharingRootFieldName(final int level, final FieldValueMapping fieldValueMapping, final String rootFieldName) {
    return fieldValueMapping.getFieldName().split("\\.").length > level && getCleanMethodName(fieldValueMapping, level).equalsIgnoreCase(rootFieldName);
  }

  public static String getCleanMethodName(final FieldValueMapping fieldValueMapping, final int level) {
    return getFullMethodName(fieldValueMapping, level).replaceAll("\\[\\d*:?]", "");
  }

  protected static String getFullMethodName(final FieldValueMapping fieldValueMapping, final int level) {
    final String pathToClean = cleanUpPath(fieldValueMapping, level);
    final int endOfField = pathToClean.contains(".") ? pathToClean.indexOf(".") : pathToClean.length();
    return pathToClean.substring(0, endOfField);
  }

  public static String cleanUpPath(final FieldValueMapping fieldValueMapping, final int level) {
    return SchemaProcessorUtils.cleanUpPathStr(fieldValueMapping.getFieldName(), level);
  }

  public static String cleanUpPathStr(final String fieldNamePath, final int level) {
    final String[] splitPath = fieldNamePath.split("\\.");
    return String.join(".", Arrays.copyOfRange(splitPath, level, splitPath.length));
  }

  public static String getConcreteLevelField(final FieldValueMapping fieldValueMapping, final int level) {
    final String[] splitPath = splitAndNormalizeFullFieldName(fieldValueMapping.getFieldName());
    return splitPath[level];
  }

  public static String removeFieldPathFirstElement(final String fieldNamePath) {
    final String returnString;
    final String[] splitPath = fieldNamePath.split("\\.");
    if (splitPath.length > 0) {
      returnString = String.join(".", Arrays.copyOfRange(splitPath, 1, splitPath.length));
    } else {
      returnString = "";
    }
    return returnString;
  }

  public static String[] splitAndNormalizeFullFieldName(final String fullFieldName) {
    final String[] fields = fullFieldName.split("\\.");
    return Arrays.stream(fields).map(field -> field.replaceAll("\\[.*]", "")).toArray(String[]::new);
  }

  public static Descriptors.Descriptor buildProtoDescriptor(final ProtoFileElement schema) throws Descriptors.DescriptorValidationException, IOException {

    final DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
    final List<String> imports = schema.getImports();
    for (String importedClass : imports) {
      try (final InputStream resourceStream = SchemaProcessorUtils.class.getClassLoader().getResourceAsStream(importedClass)) {
        if (null != resourceStream) {
          final String schemaToString = new String(resourceStream.readAllBytes());
          final var lines = new ArrayList<>(CollectionUtils.select(Arrays.asList(schemaToString.split("\\n")), isValid()));
          if (!ProtobufHelper.NOT_ACCEPTED_IMPORTS.contains(importedClass)) {
            final var importedSchema = processImported(lines);
            schemaBuilder.addDependency(importedSchema.getFileDescriptorSet().getFile(0).getName());
            schemaBuilder.addSchema(importedSchema);
          }
        }
      }
    }
    final MessageElement messageElement = (MessageElement) schema.getTypes().get(0);
    schemaBuilder.setPackage(schema.getPackageName());

    final int deepLevel = -1;
    /*
        The variable 'globalNestedTypes' records the required 'TypeElements', first by depth level, and then by Message.
     */
    final HashMap<Integer, HashMap<String, HashMap<String, TypeElement>>> globalNestedTypes = new HashMap<>();

    schemaBuilder.addMessageDefinition(buildProtoMessageDefinition(messageElement.getName(), messageElement, globalNestedTypes, deepLevel));

    return schemaBuilder.build().getMessageDescriptor(messageElement.getName());
  }

  private static Predicate<String> isValid() {
    return line -> !line.contains("//") && !line.isEmpty();
  }

  private static DynamicSchema processImported(final List<String> importedLines) throws DescriptorValidationException {

    final DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();

    String packageName = "";
    final var linesIterator = importedLines.listIterator();
    while (linesIterator.hasNext()) {
      final var fileLine = linesIterator.next();

      if (fileLine.startsWith("package")) {
        packageName = StringUtils.chop(fileLine.substring(7).trim());
        schemaBuilder.setPackage(packageName);
      }
      if (fileLine.startsWith("message")) {
        final var messageName = StringUtils.chop(fileLine.substring(7).trim()).trim();
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
    final MessageDefinition.Builder messageDefinition = MessageDefinition.newBuilder(messageName);
    while (messageLines.hasNext() && !exit) {
      final var field = messageLines.next().trim().split("\\s");
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

  private static MessageDefinition buildProtoMessageDefinition(
      final String fieldName, final TypeElement messageElement, final HashMap<Integer, HashMap<String, HashMap<String, TypeElement>>> globalNestedTypes, int deepLevel) {

    deepLevel++;
    log.info("Processing deep level {} with fieldName {}...", deepLevel, fieldName);

    fillNestedTypes(messageElement, globalNestedTypes, deepLevel);

    final MessageDefinition.Builder msgDef = MessageDefinition.newBuilder(fieldName);
    final var element = (MessageElement) messageElement;
    extracted(globalNestedTypes, msgDef, element.getFields(), deepLevel, fieldName);
    for (var optionalField : element.getOneOfs()) {
      extracted(globalNestedTypes, msgDef, optionalField.getFields(), deepLevel, fieldName);
    }
    return msgDef.build();
  }

  private static void extracted(
      final HashMap<Integer, HashMap<String, HashMap<String, TypeElement>>> globalNestedTypes, final Builder msgDef, final List<FieldElement> fieldElementList,
      final int deepLevel, final String messageName) {

    final HashMap<String, TypeElement> nestedTypes = processLevelTypes(globalNestedTypes, msgDef, fieldElementList, deepLevel,
                                                                 messageName);

    for (var elementField : fieldElementList) {
      final var elementFieldType = elementField.getType();
      final var dotType = checkDotType(elementFieldType);
      if (nestedTypes.containsKey(elementFieldType)) {

        TypeElement removed = nestedTypes.remove(dotType);
        globalNestedTypes.get(deepLevel).put(messageName, nestedTypes);

        addDefinition(msgDef, elementFieldType, removed, globalNestedTypes, deepLevel);
      }

      if (nestedTypes.containsKey(dotType)) {

        TypeElement removed = nestedTypes.remove(dotType);
        globalNestedTypes.get(deepLevel).put(messageName, nestedTypes);

        addDefinition(msgDef, dotType, removed, globalNestedTypes, deepLevel);
      }

      if (elementField.getType().startsWith("map")) {
        final var realType = StringUtils.chop(elementFieldType.substring(elementFieldType.indexOf(',') + 1).trim());
        final var mapDotType = checkDotType(realType);

        if (nestedTypes.containsKey(realType)) {

          TypeElement removed = nestedTypes.remove(dotType);
          globalNestedTypes.get(deepLevel).put(messageName, nestedTypes);

          addDefinition(msgDef, realType, removed, globalNestedTypes, deepLevel);
        }

        if (nestedTypes.containsKey(mapDotType)) {

          TypeElement removed = nestedTypes.remove(dotType);
          globalNestedTypes.get(deepLevel).put(messageName, nestedTypes);

          addDefinition(msgDef, mapDotType, removed, globalNestedTypes, deepLevel);
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

  @NotNull
  private static HashMap<String, TypeElement> processLevelTypes(
      final HashMap<Integer, HashMap<String, HashMap<String, TypeElement>>> globalNestedTypes, final Builder msgDef, final List<FieldElement> fieldElementList, final int deepLevel,
      final String messageName) {

    /*
    We store in a list, all the types instantiated by the attributes.
     */
    List<String> typesWithSimpleNames = new ArrayList<>();
    for (FieldElement fieldElement : fieldElementList) {

      final String typeCompletePath = fieldElement.getType();
      final String typeSimple = checkDotType(typeCompletePath);

      if (typeSimple != null && !typeSimple.isEmpty()) {
        typesWithSimpleNames.add(typeSimple);
      }
    }

    List<String> nestedTypesToDelete = new ArrayList<>();
    HashMap<String, TypeElement> nestedTypes = globalNestedTypes.get(deepLevel).get(messageName);

    /*
    Loop through the set of types declared at the current depth level.
     */
    for (Map.Entry<String, TypeElement> entry : nestedTypes.entrySet()) {
      final String fieldName = entry.getKey();
      final TypeElement typeElement = entry.getValue();

      if (!typesWithSimpleNames.contains(fieldName)) {
        /*
        Just in case a type declared at this level of depth is not used by any attribute, we add it to the definition.
         */
        log.info("{} not contains the nestedType [{}]. We proceed to add it to the definition.", typesWithSimpleNames.toString(), fieldName);
        nestedTypesToDelete.add(fieldName);
        addDefinition(msgDef, fieldName, typeElement, globalNestedTypes, deepLevel);
      }
    }

    // Delete all the nestedSubTypes processed from de global Map collection.
    nestedTypesToDelete.forEach(nestedTypes::remove);

    globalNestedTypes.get(deepLevel).put(messageName, nestedTypes);
    return nestedTypes;
  }

  private static void addDefinition(
      final MessageDefinition.Builder msgDef, final String typeName, final TypeElement typeElement,
      final HashMap<Integer, HashMap<String, HashMap<String, TypeElement>>> globalNestedTypes, final int deepLevel) {

    if (typeElement instanceof EnumElement) {
      final var enumElement = (EnumElement) typeElement;
      final EnumDefinition.Builder builder = EnumDefinition.newBuilder(enumElement.getName());
      for (var constant : enumElement.getConstants()) {
        builder.addValue(constant.getName(), constant.getTag());
      }
      msgDef.addEnumDefinition(builder.build());
    } else {
      if (!typeName.contains(".")) {
        msgDef.addMessageDefinition(buildProtoMessageDefinition(typeName, typeElement, globalNestedTypes, deepLevel));
      }
    }
  }

  private static void fillNestedTypes(
      final TypeElement messageElement, final HashMap<Integer, HashMap<String, HashMap<String, TypeElement>>> globalNestedTypes,
      final int deepLevel) {

    HashMap<String, HashMap<String, TypeElement>> messageNestedTypes = globalNestedTypes.get(deepLevel);
    if (messageNestedTypes == null) {
      messageNestedTypes = new HashMap<>();
    }

    HashMap<String, TypeElement> nestedTypes = new HashMap<>();
    messageElement.getNestedTypes().forEach(nestedType -> nestedTypes.put(nestedType.getName(), nestedType));

    messageNestedTypes.put(messageElement.getName(), nestedTypes);
    globalNestedTypes.put(deepLevel, messageNestedTypes);
  }

  private static String checkDotType(final String subfieldType) {
    String dotType = "";
    if (subfieldType.startsWith(".") || subfieldType.contains(".")) {
      final String[] typeSplit = subfieldType.split("\\.");
      dotType = typeSplit[typeSplit.length - 1];
    }
    return dotType;
  }

  public static String getOneDimensionValueType(final String completeValueType) {
    final int numberOfHyphens = StringUtils.countMatches(completeValueType, "-");
    final String[] types = completeValueType.split("-");
    return numberOfHyphens > 1 ? String.join("-", Arrays.copyOfRange(types, 0, 2)) : completeValueType;
  }

  public static boolean checkIfOptionalCollection(final FieldValueMapping field, final int level) {
    return !field.getRequired() && field.getFieldValuesList().contains("null") && hasMapOrArrayTypeFilter(getCleanMethodName(field, level));
  }

  public static boolean hasMapOrArrayTypeFilter(final String typeFilter) {
    return typeFilter.matches("\\[.*].*");
  }

  public static boolean isLastLevel(final FieldValueMapping field, final int level) {
    final String[] fieldParts = field.getFieldName().split("\\.");
    return fieldParts.length - 1 == level;
  }

  public static boolean isFieldValueListNotAcceptingNullValues(final List<String> fieldValueList) {
    return !fieldValueList.contains("null");
  }
}
