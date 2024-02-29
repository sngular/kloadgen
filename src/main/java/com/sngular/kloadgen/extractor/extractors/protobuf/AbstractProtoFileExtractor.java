package com.sngular.kloadgen.extractor.extractors.protobuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.Descriptors;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.util.ProtobufHelper;
import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.internal.parser.EnumConstantElement;
import com.squareup.wire.schema.internal.parser.EnumElement;
import com.squareup.wire.schema.internal.parser.FieldElement;
import com.squareup.wire.schema.internal.parser.MessageElement;
import com.squareup.wire.schema.internal.parser.OneOfElement;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.TypeElement;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractProtoFileExtractor {

  public static final String ARRAY_POSTFIX = "-array";

  public static final String MAP_POSTFIX = "-map";

  public static final String UNSUPPORTED_TYPE_OF_VALUE = "Something Odd Just Happened: Unsupported type of value";

  protected AbstractProtoFileExtractor() {
  }

  public final List<FieldValueMapping> processSchemaDefault(final ProtoFileElement schema) {
    final List<FieldValueMapping> attributeList = new ArrayList<>();
    final HashMap<String, TypeElement> nestedTypes = new HashMap<>();
    schema.getTypes().forEach(field -> processField(field, attributeList, schema.getImports(), true, nestedTypes));
    return attributeList;
  }

  public final List<String> getSchemaNameListDefault(final String schema) {
    final DynamicSchema dynamicSchema;
    try {
      dynamicSchema = DynamicSchema.parseFrom(schema.getBytes(StandardCharsets.UTF_8));
    } catch (Descriptors.DescriptorValidationException | IOException e) {
      throw new KLoadGenException(e);
    }
    return new ArrayList<>(dynamicSchema.getMessageTypes());
  }

  public static void processField(
      final TypeElement field, final List<FieldValueMapping> completeFieldList,
      final List<String> imports, final boolean isAncestorRequired,
      final Map<String, TypeElement> nestedTypes) {
    fillNestedTypes(field, nestedTypes);
    if (field instanceof final MessageElement messageField) {
      if (!messageField.getOneOfs().isEmpty()) {
        extractOneOfs((MessageElement) field, completeFieldList, nestedTypes, isAncestorRequired);
      }
      for (var subfield : messageField.getFields()) {
        processField(subfield, completeFieldList, imports, isAncestorRequired, nestedTypes);
      }
    } else if (field instanceof EnumElement) {
      final var values = extractEnums((EnumElement) field);
      if (StringUtils.isNotEmpty(values)) {
        completeFieldList.add(FieldValueMapping.builder().fieldName("").fieldType("enum").valueLength(0).fieldValueList(values).required(true).isAncestorRequired(true).build());
      }
    } else {
      throw new KLoadGenException(UNSUPPORTED_TYPE_OF_VALUE);
    }
  }

  private static void processField(final FieldElement field, final List<FieldValueMapping> completeFieldList, final List<String> imports, final boolean isAncestorRequired,
      final Map<String, TypeElement> nestedTypes) {
    final Field.Label label = checkNullLabel(field);
    final boolean isArray = "repeated".equalsIgnoreCase(Objects.requireNonNull(label.toString()));
    final boolean isOptional = Objects.equals(Objects.requireNonNull(label.toString()), "optional");
    final boolean isMap = field.getType().startsWith("map");
    if (ProtobufHelper.isValidType(field.getType())) {
      extractPrimitiveTypes(completeFieldList, field, isArray, !isOptional, isAncestorRequired);
    } else if (isMap) {
      extractMapType(completeFieldList, nestedTypes, field, imports, !isOptional, isAncestorRequired);
    } else {
      final String dotType = checkDotType(field.getType(), imports);
      if (nestedTypes.containsKey(field.getType())) {
        extractNestedTypes(completeFieldList, nestedTypes, field, isArray, imports, !isOptional, isAncestorRequired);
      } else if (nestedTypes.containsKey(dotType)) {
        completeFieldList.addAll(extractDotTypesWhenIsInNestedType(nestedTypes, field, isArray, dotType, imports, !isOptional, isAncestorRequired));
      } else {
        completeFieldList.add(extractDotTypeWhenNotNestedType(field, isArray, dotType, !isOptional, isAncestorRequired));
      }
    }
  }

  private static List<FieldValueMapping> processFieldList(final TypeElement fieldList, final List<String> imports, final Map<String, TypeElement> nestedTypes) {
    final List<FieldValueMapping> completeFieldList = new ArrayList<>();
    processField(fieldList, completeFieldList, imports, false, nestedTypes);
    return completeFieldList;
  }

  private static void extractOneOfs(final MessageElement field, final List<FieldValueMapping> completeFieldList, final Map<String, TypeElement> nestedTypes,
      final boolean isAncestorRequired) {
    final List<OneOfElement> oneOfs = new ArrayList<>(field.getOneOfs());
    for (OneOfElement oneOfElement : oneOfs) {
      if (!oneOfElement.getFields().isEmpty()) {
        final FieldElement subField = oneOfElement.getFields().get(RandomUtils.nextInt(0, oneOfElement.getFields().size()));
        if (ProtobufHelper.isValidType(subField.getType())) {
          completeFieldList.add(FieldValueMapping.builder().fieldName(subField.getName()).fieldType(ProtobufHelper.translateType(subField.getType())).required(true)
                                                 .isAncestorRequired(isAncestorRequired).build());
        } else if (nestedTypes.containsKey(subField.getType())) {
          final MessageElement clonedField = new MessageElement(field.getLocation(), field.getName(), field.getDocumentation(), field.getNestedTypes(), field.getOptions(),
                                                                field.getReserveds(), oneOfElement.getFields(), Collections.emptyList(), field.getExtensions(), field.getGroups(),
                                                                field.getExtendDeclarations());
          processField(clonedField, completeFieldList, Collections.emptyList(), isAncestorRequired, nestedTypes);
        } else {
          completeFieldList.add(
              FieldValueMapping.builder().fieldName(subField.getName()).fieldType(subField.getType()).required(true).isAncestorRequired(isAncestorRequired).build());
        }

      }
    }
  }

  private static FieldValueMapping extractDotTypeWhenNotNestedType(final FieldElement subfield, final boolean isArray, final String dotType, final boolean isRequired,
      final boolean isAncestorRequired) {
    final FieldValueMapping completeFieldList;
    if (isArray) {
      completeFieldList =
          FieldValueMapping.builder().fieldName(subfield.getName() + "[]").fieldType(dotType + ARRAY_POSTFIX).required(isRequired).isAncestorRequired(isAncestorRequired).build();
    } else {
      completeFieldList = FieldValueMapping.builder().fieldName(subfield.getName()).fieldType(dotType).required(isRequired).isAncestorRequired(isAncestorRequired).build();
    }
    return completeFieldList;
  }

  private static void extractMapType(final List<FieldValueMapping> completeFieldList, final Map<String, TypeElement> nestedTypes, final FieldElement subfield,
      final List<String> imports, final boolean isRequired, final boolean isAncestorRequired) {
    final String subFieldType = extractInternalMapFields(subfield);
    final String dotTypeMap = checkDotType(subFieldType, imports);
    if (ProtobufHelper.isValidType(subFieldType)) {
      completeFieldList.add(
          FieldValueMapping.builder().fieldName(subfield.getName() + "[:]").fieldType(subFieldType.replace(subFieldType, ProtobufHelper.translateType(subFieldType)) + MAP_POSTFIX)
                           .required(isRequired).isAncestorRequired(isAncestorRequired).build());
    } else if (nestedTypes.containsKey(subFieldType)) {
      extractNestedTypesMap(completeFieldList, nestedTypes, subfield, imports, isRequired, isAncestorRequired);
    } else if (nestedTypes.containsKey(dotTypeMap)) {
      extractDotTypesMap(completeFieldList, nestedTypes, subfield, dotTypeMap, imports, isRequired, isAncestorRequired);
    } else if (!imports.isEmpty() && isExternalType(imports, dotTypeMap)) {
      completeFieldList.add(FieldValueMapping.builder().fieldName(subfield.getName() + "[:]").fieldType(ProtobufHelper.translateType("string") + MAP_POSTFIX).required(isRequired)
                                             .isAncestorRequired(isAncestorRequired).build());
    } else {
      throw new KLoadGenException(UNSUPPORTED_TYPE_OF_VALUE);
    }
  }

  private static String extractInternalMapFields(final FieldElement subfield) {
    final String[] mapSplit = subfield.getType().split(",");
    return mapSplit[1].replace(">", "").trim();
  }

  private static void extractPrimitiveTypes(final List<FieldValueMapping> completeFieldList, final FieldElement subfield, final boolean isArray, final boolean isRequired,
      final boolean isAncestorRequired) {
    if (isArray) {
      completeFieldList.add(FieldValueMapping.builder().fieldName(subfield.getName() + "[]")
                                             .fieldType(subfield.getType().replace(subfield.getType(), ProtobufHelper.translateType(subfield.getType())) + ARRAY_POSTFIX)
                                             .required(isRequired).isAncestorRequired(isAncestorRequired).build());
    } else {
      completeFieldList.add(
          FieldValueMapping.builder().fieldName(subfield.getName()).fieldType(subfield.getType().replace(subfield.getType(), ProtobufHelper.translateType(subfield.getType())))
                           .required(isRequired).isAncestorRequired(isAncestorRequired).build());
    }
  }

  private static List<FieldValueMapping> extractDotTypesWhenIsInNestedType(
      final Map<String, TypeElement> nestedTypes, final FieldElement subfield, final boolean isArray, final String dotType,
      final List<String> imports, final boolean isRequired, final boolean isAncestorRequired) {
    final List<FieldValueMapping> finalFieldValueMapping = new ArrayList<>();
    final List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(dotType), imports, nestedTypes);
    for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
      if (isArray) {
        finalFieldValueMapping.add(
            FieldValueMapping.builder().fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "[].")).fieldType(fieldValueMapping.getFieldType())
                             .valueLength(0).fieldValueList(getValueList(fieldValueMapping)).required(isRequired).isAncestorRequired(isAncestorRequired).build());
      } else {
        finalFieldValueMapping.add(
            FieldValueMapping.builder().fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), ".")).fieldType(fieldValueMapping.getFieldType())
                             .valueLength(0).fieldValueList(getValueList(fieldValueMapping)).required(isRequired).isAncestorRequired(isAncestorRequired).build());
      }
    }
    return finalFieldValueMapping;
  }

  private static void extractNestedTypes(
      final List<FieldValueMapping> completeFieldList, final Map<String, TypeElement> nestedTypes, final FieldElement subfield, final boolean isArray,
      final List<String> imports, final boolean isRequired, final boolean isAncestorRequired) {
    final List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(subfield.getType()), imports, nestedTypes);
    for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
      if ("enum".equals(fieldValueMapping.getFieldType())) {
        if (isArray) {
          completeFieldList.add(FieldValueMapping.builder().fieldName(subfield.getName() + "[]").fieldType(fieldValueMapping.getFieldType() + ARRAY_POSTFIX).valueLength(0)
                                                 .fieldValueList(getValueList(fieldValueMapping)).required(isRequired).isAncestorRequired(isAncestorRequired).build());
        } else {
          completeFieldList.add(
              FieldValueMapping.builder().fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), ".")).fieldType(fieldValueMapping.getFieldType())
                               .valueLength(0).fieldValueList(getValueList(fieldValueMapping)).required(isRequired).isAncestorRequired(isAncestorRequired).build());
        }
      } else {
        if (isArray) {
          completeFieldList.add(
              FieldValueMapping.builder().fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "[].")).fieldType(fieldValueMapping.getFieldType())
                               .required(isRequired).isAncestorRequired(isAncestorRequired).build());
        } else {
          completeFieldList.add(
              FieldValueMapping.builder().fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), ".")).fieldType(fieldValueMapping.getFieldType())
                               .required(isRequired).isAncestorRequired(isAncestorRequired).build());
        }
      }
    }
  }

  private static String buildFieldName(final String fieldName, final String lastFieldName, final String splitter) {
    return StringUtils.isNotEmpty(lastFieldName) ? fieldName + splitter + lastFieldName : fieldName;
  }

  private static void extractNestedTypesMap(
      final List<FieldValueMapping> completeFieldList, final Map<String, TypeElement> nestedTypes, final FieldElement subfield, final List<String> imports,
      final boolean isRequired, final boolean isAncestorRequired) {
    final List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(extractInternalMapFields(subfield)), imports, nestedTypes);
    for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
      if ("enum".equals(fieldValueMapping.getFieldType())) {
        completeFieldList.add(FieldValueMapping.builder().fieldName(subfield.getName() + "[:]").fieldType(fieldValueMapping.getFieldType() + MAP_POSTFIX).valueLength(0)
                                               .fieldValueList(getValueList(fieldValueMapping)).required(isRequired).isAncestorRequired(isAncestorRequired).build());
      } else {
        completeFieldList.add(
            FieldValueMapping.builder().fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "[:].")).fieldType(fieldValueMapping.getFieldType())
                             .required(isRequired).isAncestorRequired(isAncestorRequired).build());
      }
    }
  }

  private static String getValueList(final FieldValueMapping fieldValueMapping) {
    final var valueList = fieldValueMapping.getFieldValuesList().toString();
    return "[]".equalsIgnoreCase(valueList) ? "" : valueList;
  }

  private static void extractDotTypesMap(
      final List<FieldValueMapping> completeFieldList, final Map<String, TypeElement> nestedTypes, final FieldElement subfield, final String dotType,
      final List<String> imports, final boolean isRequired, final boolean isAncestorRequired) {
    final List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(dotType), imports, nestedTypes);
    for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
      completeFieldList.add(
          FieldValueMapping.builder().fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "[:].")).fieldType(fieldValueMapping.getFieldType())
                           .required(isRequired).isAncestorRequired(isAncestorRequired).build());

    }
  }

  private static String extractEnums(final EnumElement field) {
    final var enumConstantList = CollectionUtils.collect(field.getConstants(), EnumConstantElement::getName);
    return String.join(",", enumConstantList);
  }

  private static String checkDotType(final String subfieldType, final List<String> imports) {
    String dotType = subfieldType;
    if (subfieldType.startsWith(".") || subfieldType.contains(".")) {
      final String[] typeSplit = subfieldType.split("\\.");
      dotType = typeSplit[typeSplit.length - 1];
      dotType = !isExternalType(imports, dotType) ? dotType : subfieldType;
    }
    return dotType;
  }

  private static boolean isExternalType(final List<String> imports, final String fieldType) {
    boolean externalType = false;
    for (String importType : imports) {
      final Pattern pattern = Pattern.compile("(/([^/]+)\\.)");
      final Matcher matcher = pattern.matcher(importType);
      if (matcher.find()) {
        final String extractedImportType = matcher.group(2);
        if (extractedImportType != null) {
          if (extractedImportType.toLowerCase().contains(fieldType.toLowerCase()) || fieldType.toLowerCase().contains(extractedImportType.toLowerCase())) {
            externalType = true;
          } else {
            if (isExternalTypeByURL(importType, fieldType)) {
              externalType = true;
            }
          }
        }
      }
    }
    return externalType;
  }

  private static boolean isExternalTypeByURL(final String importType, final String fieldType) {
    String[] importTypeSplitted = importType.split("/");
    importTypeSplitted = Arrays.copyOf(importTypeSplitted, importTypeSplitted.length - 1);
    String[] fieldTypeSplitted = fieldType.split("\\.");
    fieldTypeSplitted = Arrays.copyOf(fieldTypeSplitted, fieldTypeSplitted.length - 1);
    final String stringImportSplitted = String.join(".", importTypeSplitted);
    final String stringFieldSplitted = String.join(".", fieldTypeSplitted);

    return stringFieldSplitted.equals(stringImportSplitted);
  }

  private static Field.Label checkNullLabel(final FieldElement subfield) {
    final Field.Label label;
    if (Objects.nonNull(subfield.getLabel())) {
      label = subfield.getLabel();
    } else {
      label = Field.Label.OPTIONAL;
    }
    return label;
  }

  private static void fillNestedTypes(final TypeElement field, final Map<String, TypeElement> nestedTypes) {
    if (!field.getNestedTypes().isEmpty()) {
      for (var nestedField : field.getNestedTypes()) {
        nestedTypes.put(nestedField.getName(), nestedField);
      }
    }
  }
}
