package net.coru.kloadgen.extractor.extractors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.internal.parser.EnumConstantElement;
import com.squareup.wire.schema.internal.parser.EnumElement;
import com.squareup.wire.schema.internal.parser.FieldElement;
import com.squareup.wire.schema.internal.parser.MessageElement;
import com.squareup.wire.schema.internal.parser.OneOfElement;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.TypeElement;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.ProtobufHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class ProtoBufExtractor {

  public static final String ARRAY_POSTFIX = "-array";

  public static final String MAP_POSTFIX = "-map";

  public static final String UNSUPPORTED_TYPE_OF_VALUE = "Something Odd Just Happened: Unsupported type of value";

  public List<FieldValueMapping> processSchema(ProtoFileElement schema) {
    List<FieldValueMapping> attributeList = new ArrayList<>();
    schema.getTypes().forEach(field -> processField(field, attributeList, schema.getImports(), true));
    return attributeList;
  }

  public void processField(TypeElement field, List<FieldValueMapping> completeFieldList, List<String> imports, final boolean isAncestorRequired) {
    HashMap<String, TypeElement> nestedTypes = new HashMap<>();
    fillNestedTypes(field, nestedTypes);
    if (field instanceof MessageElement) {
      var messageField = (MessageElement) field;
      if (!messageField.getOneOfs().isEmpty()) {
        extractOneOfs((MessageElement) field, completeFieldList, nestedTypes, isAncestorRequired);
      }
      for (var subfield : messageField.getFields()) {
        Field.Label label = checkNullLabel(subfield);
        boolean isArray = "repeated".equalsIgnoreCase(Objects.requireNonNull(label.toString()));
        boolean isOptional = "optional".equals(Objects.requireNonNull(label.toString()));
        boolean isMap = subfield.getType().startsWith("map");
        if (ProtobufHelper.isValidType(subfield.getType())) {
          extractPrimitiveTypes(completeFieldList, subfield, isArray, !isOptional, isAncestorRequired);
        } else if (isMap) {
          extractMapType(completeFieldList, nestedTypes, subfield, imports, !isOptional, isAncestorRequired);
        } else {
          String dotType = checkDotType(subfield.getType(), imports);
          if (nestedTypes.containsKey(subfield.getType())) {
            extractNestedTypes(completeFieldList, nestedTypes, subfield, isArray, imports, !isOptional, isAncestorRequired);
          } else if (nestedTypes.containsKey(dotType)) {
            extractDotTypesWhenIsInNestedType(completeFieldList, nestedTypes, subfield, isArray, dotType, imports, !isOptional, isAncestorRequired);
          } else {
            extractDotTypeWhenNotNestedType(completeFieldList, subfield, isArray, dotType, !isOptional, isAncestorRequired);
          }
        }
      }
    } else if (field instanceof EnumElement) {
      var values = extractEnums((EnumElement) field);
      if (StringUtils.isNotEmpty(values)) {
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName("")
                             .fieldType("enum")
                             .valueLength(0)
                             .fieldValueList(values)
                             .build());
      }
    } else {
      throw new KLoadGenException(UNSUPPORTED_TYPE_OF_VALUE);
    }
  }

  private List<FieldValueMapping> processFieldList(TypeElement fieldList, List<String> imports) {
    List<FieldValueMapping> completeFieldList = new ArrayList<>();
    processField(fieldList, completeFieldList, imports, false);
    return completeFieldList;
  }

  private void extractOneOfs(MessageElement field, List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, final boolean isAncestorRequired) {
    List<OneOfElement> oneOfs = new ArrayList<>(field.getOneOfs());
    for (OneOfElement oneOfElement : oneOfs) {
      if (!oneOfElement.getFields().isEmpty()) {
        FieldElement subField = oneOfElement.getFields().get(RandomUtils.nextInt(0, oneOfElement.getFields().size()));
        if (ProtobufHelper.isValidType(subField.getType())) {
          completeFieldList.add(
              FieldValueMapping.builder()
                               .fieldName(subField.getName())
                               .fieldType(ProtobufHelper.translateType(subField.getType()))
                               .valueLength(0)
                               .fieldValueList("")
                               .required(true)
                               .isAncestorRequired(isAncestorRequired)
                               .build());
        } else if (nestedTypes.containsKey(subField.getType())) {
          MessageElement clonedField = new MessageElement(field.getLocation(), field.getName(), field.getDocumentation(),
                                                          field.getNestedTypes(), field.getOptions(), field.getReserveds(), oneOfElement.getFields(), Collections.emptyList(),
                                                          field.getExtensions(), field.getGroups());
          processField(clonedField, completeFieldList, Collections.emptyList(), isAncestorRequired);
        } else {
          completeFieldList.add(
              FieldValueMapping.builder()
                               .fieldName(subField.getName())
                               .fieldType(subField.getType())
                               .valueLength(0)
                               .fieldValueList("")
                               .required(true)
                               .isAncestorRequired(isAncestorRequired)
                               .build());
        }
      }
    }
  }

  private void extractDotTypeWhenNotNestedType(
      List<FieldValueMapping> completeFieldList, FieldElement subfield, boolean isArray, String dotType, final boolean isRequired, final boolean isAncestorRequired) {
    if (isArray) {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(subfield.getName() + "[]")
                           .fieldType(dotType + ARRAY_POSTFIX)
                           .valueLength(0)
                           .fieldValueList("")
                           .required(isRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build());
    } else {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(subfield.getName())
                           .fieldType(dotType)
                           .valueLength(0)
                           .fieldValueList("")
                           .required(isRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build());
    }
  }

  private void extractMapType(
      List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, List<String> imports, final boolean isRequired,
      final boolean isAncestorRequired) {
    String subFieldType = extractInternalMapFields(subfield);
    String dotTypeMap = checkDotType(subFieldType, imports);
    if (ProtobufHelper.isValidType(subFieldType)) {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(subfield.getName() + "[:]")
                           .fieldType(subFieldType.replace(subFieldType, ProtobufHelper.translateType(subFieldType)) + MAP_POSTFIX)
                           .valueLength(0)
                           .fieldValueList("")
                           .required(isRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build());
    } else if (nestedTypes.containsKey(subFieldType)) {
      extractNestedTypesMap(completeFieldList, nestedTypes, subfield, imports, isRequired, isAncestorRequired);
    } else if (nestedTypes.containsKey(dotTypeMap)) {
      extractDotTypesMap(completeFieldList, nestedTypes, subfield, dotTypeMap, imports, isRequired, isAncestorRequired);
    } else if (!imports.isEmpty() && isExternalType(imports, dotTypeMap)) {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(subfield.getName() + "[:]")
                           .fieldType(ProtobufHelper.translateType("string") + MAP_POSTFIX)
                           .valueLength(0)
                           .fieldValueList("")
                           .required(isRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build());
    } else {
      throw new KLoadGenException(UNSUPPORTED_TYPE_OF_VALUE);
    }
  }

  private String extractInternalMapFields(FieldElement subfield) {
    String[] mapSplit = subfield.getType().split(",");
    return mapSplit[1].replace(">", "").trim();
  }

  private void extractPrimitiveTypes(List<FieldValueMapping> completeFieldList, FieldElement subfield, boolean isArray, final boolean isRequired, final boolean isAncestorRequired) {
    if (isArray) {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(subfield.getName() + "[]")
                           .fieldType(subfield.getType().replace(subfield.getType(),ProtobufHelper.translateType(subfield.getType())) + ARRAY_POSTFIX)
                           .valueLength(0)
                           .fieldValueList("")
                           .required(isRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build());
    } else {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(subfield.getName())
                           .fieldType(subfield.getType().replace(subfield.getType(), ProtobufHelper.translateType(subfield.getType())))
                           .valueLength(0)
                           .fieldValueList("")
                           .required(isRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build());
    }
  }

  private void extractDotTypesWhenIsInNestedType(
      List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, boolean isArray, String dotType,
      List<String> imports, final boolean isRequired, final boolean isAncestorRequired) {
    List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(dotType), imports);
    for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
      if (isArray) {
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "[]."))
                             .fieldType(fieldValueMapping.getFieldType())
                             .valueLength(0)
                             .fieldValueList(getValueList(fieldValueMapping))
                             .required(isRequired)
                             .isAncestorRequired(isAncestorRequired)
                             .build());
      } else {
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "."))
                             .fieldType(fieldValueMapping.getFieldType())
                             .valueLength(0)
                             .fieldValueList(getValueList(fieldValueMapping))
                             .required(isRequired)
                             .isAncestorRequired(isAncestorRequired)
                             .build());
      }
    }
  }

  private void extractNestedTypes(
      List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, boolean isArray, List<String> imports, final boolean isRequired,
      final boolean isAncestorRequired) {
    List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(subfield.getType()), imports);
    for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
      if ("enum".equals(fieldValueMapping.getFieldType())) {
        if (isArray) {
          completeFieldList.add(
              FieldValueMapping.builder()
                               .fieldName(subfield.getName() + "[]")
                               .fieldType(fieldValueMapping.getFieldType() + ARRAY_POSTFIX)
                               .valueLength(0)
                               .fieldValueList(getValueList(fieldValueMapping))
                               .required(isRequired)
                               .isAncestorRequired(isAncestorRequired)
                               .build());
        } else {
          completeFieldList.add(
              FieldValueMapping.builder()
                               .fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "."))
                               .fieldType(fieldValueMapping.getFieldType())
                               .valueLength(0)
                               .fieldValueList(getValueList(fieldValueMapping))
                               .required(isRequired)
                               .isAncestorRequired(isAncestorRequired)
                               .build());
        }
      } else {
        if (isArray) {
          completeFieldList.add(
              FieldValueMapping.builder()
                               .fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "[]."))
                               .fieldType(fieldValueMapping.getFieldType())
                               .valueLength(0)
                               .fieldValueList("")
                               .required(isRequired)
                               .isAncestorRequired(isAncestorRequired)
                               .build());
        } else {
          completeFieldList.add(
              FieldValueMapping.builder()
                               .fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "."))
                               .fieldType(fieldValueMapping.getFieldType())
                               .valueLength(0)
                               .fieldValueList("")
                               .required(isRequired)
                               .isAncestorRequired(isAncestorRequired)
                               .build());
        }
      }
    }
  }

  private String buildFieldName(String fieldName, String lastFieldName, String splitter) {
    return StringUtils.isNotEmpty(lastFieldName) ? fieldName + splitter + lastFieldName : fieldName;
  }

  private void extractNestedTypesMap(
      List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, List<String> imports, final boolean isRequired,
      final boolean isAncestorRequired) {
    List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(extractInternalMapFields(subfield)), imports);
    for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
      if ("enum".equals(fieldValueMapping.getFieldType())) {
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(subfield.getName() + "[:]")
                             .fieldType(fieldValueMapping.getFieldType() + MAP_POSTFIX)
                             .valueLength(0)
                             .fieldValueList(getValueList(fieldValueMapping))
                             .required(isRequired)
                             .isAncestorRequired(isAncestorRequired)
                             .build());
      } else {
        completeFieldList.add(
            FieldValueMapping.builder()
                             .fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "[:]."))
                             .fieldType(fieldValueMapping.getFieldType())
                             .valueLength(0)
                             .fieldValueList("")
                             .required(isRequired)
                             .isAncestorRequired(isAncestorRequired)
                             .build());
      }
    }
  }

  private String getValueList(final FieldValueMapping fieldValueMapping) {
    var valueList = fieldValueMapping.getFieldValuesList().toString();
    return "[]".equalsIgnoreCase(valueList) ? "" : valueList;
  }

  private void extractDotTypesMap(
      List<FieldValueMapping> completeFieldList, HashMap<String, TypeElement> nestedTypes, FieldElement subfield, String dotType, List<String> imports, final boolean isRequired,
      final boolean isAncestorRequired) {
    List<FieldValueMapping> fieldValueMappingList = processFieldList(nestedTypes.get(dotType), imports);
    for (FieldValueMapping fieldValueMapping : fieldValueMappingList) {
      completeFieldList.add(
          FieldValueMapping.builder()
                           .fieldName(buildFieldName(subfield.getName(), fieldValueMapping.getFieldName(), "[:]."))
                           .fieldType(fieldValueMapping.getFieldType())
                           .valueLength(0)
                           .fieldValueList("")
                           .required(isRequired)
                           .isAncestorRequired(isAncestorRequired)
                           .build());

    }
  }

  private String extractEnums(EnumElement field) {
    var enumConstantList = CollectionUtils.collect(field.getConstants(), EnumConstantElement::getName);
    return String.join(",", enumConstantList);
  }

  @NotNull
  private String getFieldValueMappingPrepared(FieldValueMapping fieldValueMapping) {
    String[] splitText = fieldValueMapping.getFieldName().split("\\.");
    List<String> preparedField = Arrays.asList((Arrays.copyOfRange(splitText, 1, splitText.length)));
    return String.join(".", preparedField);
  }

  private String checkDotType(String subfieldType, List<String> imports) {
    String dotType = subfieldType;
    if (subfieldType.startsWith(".") || subfieldType.contains(".")) {
      String[] typeSplit = subfieldType.split("\\.");
      dotType = typeSplit[typeSplit.length - 1];
      dotType = !isExternalType(imports, dotType) ? dotType : subfieldType;
    }
    return dotType;
  }

  private boolean isExternalType(List<String> imports, String fieldType) {
    for (String importType : imports) {
      Pattern pattern = Pattern.compile("(/([^/]+)\\.)");
      Matcher matcher = pattern.matcher(importType);
      if (matcher.find()) {
        String extractedImportType = matcher.group(2);
        if (extractedImportType != null) {
          if (extractedImportType.toLowerCase().contains(fieldType.toLowerCase())
              || fieldType.toLowerCase().contains(extractedImportType.toLowerCase())) {
            return true;
          } else {
            if (isExternalTypeByURL(importType, fieldType)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean isExternalTypeByURL(String importType, String fieldType) {
    String[] importTypeSplitted = importType.split("/");
    importTypeSplitted = Arrays.copyOf(importTypeSplitted, importTypeSplitted.length - 1);
    String[] fieldTypeSplitted = fieldType.split("\\.");
    fieldTypeSplitted = Arrays.copyOf(fieldTypeSplitted, fieldTypeSplitted.length - 1);
    String stringImportSplitted = String.join(".", importTypeSplitted);
    String stringFieldSplitted = String.join(".", fieldTypeSplitted);

    return stringFieldSplitted.equals(stringImportSplitted);
  }

  private Field.Label checkNullLabel(FieldElement subfield) {
    Field.Label label;
    if (Objects.nonNull(subfield.getLabel())) {
      label = subfield.getLabel();
    } else {
      label = Field.Label.OPTIONAL;
    }
    return label;
  }

  private void fillNestedTypes(TypeElement field, HashMap<String, TypeElement> nestedTypes) {
    if (!field.getNestedTypes().isEmpty()) {
      for (var nestedField : field.getNestedTypes()) {
        nestedTypes.put(nestedField.getName(), nestedField);
      }
    }
  }
}