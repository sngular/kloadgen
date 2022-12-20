package com.sngular.kloadgen.processor.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;

public class JsonSchemaFixturesConstants {

  public static final List<FieldValueMapping> SIMPLE_SCHEMA = new ArrayList<FieldValueMapping>(Arrays.asList(
      FieldValueMapping.builder()
                       .fieldName("firstName")
                       .fieldType("string")
                       .valueLength(0)
                       .fieldValueList("null")
                       .required(false)
                       .isAncestorRequired(false)
                       .constraints(Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0"))
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("lastName")
                       .fieldType("string")
                       .valueLength(0)
                       .fieldValueList("García")
                       .required(true)
                       .constraints(Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0"))
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("age")
                       .fieldType("number")
                       .valueLength(0)
                       .fieldValueList("null")
                       .required(false)
                       .constraints(Collections.emptyMap())
                       .build()
  ));

  public static final String SIMPLE_SCHEMA_EXPECTED = "{\"lastName\":\"García\"}";

  public static final List<FieldValueMapping> SIMPLE_SCHEMA_MAP = new ArrayList<>(Arrays.asList(
      FieldValueMapping.builder()
                       .fieldName("firstName")
                       .fieldType("string")
                       .valueLength(0)
                       .fieldValueList("null")
                       .required(false)
                       .isAncestorRequired(false)
                       .constraints(Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0"))
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("lastName")
                       .fieldType("string")
                       .valueLength(0)
                       .fieldValueList("García")
                       .required(true)
                       .constraints(Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0"))
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("age")
                       .fieldType("number")
                       .valueLength(0)
                       .fieldValueList("null")
                       .required(false)
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("testMap.itemType[:]")
                       .fieldType("number-map")
                       .valueLength(0)
                       .fieldValueList("null")
                       .required(true)
                       .isAncestorRequired(true)
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("testMap.itemTipo[:]")
                       .fieldType("string-map")
                       .valueLength(0)
                       .fieldValueList("null")
                       .required(true)
                       .isAncestorRequired(true)
                       .build()
  ));

  public static final List<FieldValueMapping> SCHEMA_NESTED_COLLECTIONS = new ArrayList<>(Arrays.asList(
      FieldValueMapping.builder()
                       .fieldName("fruits[][:]")
                       .fieldType("string-map-array")
                       .valueLength(0)
                       .required(false)
                       .isAncestorRequired(true)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("vegetables[:][]")
                       .fieldType("string-array-map")
                       .valueLength(0)
                       .required(false)
                       .isAncestorRequired(true)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("birds[][]")
                       .fieldType("string-array-array")
                       .valueLength(0)
                       .required(true)
                       .isAncestorRequired(false)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("animals[:][:]")
                       .fieldType("string-map-map")
                       .valueLength(0)
                       .required(true)
                       .isAncestorRequired(false)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build()
  ));

  public static final List<FieldValueMapping> SCHEMA_COMPLEX_COLLECTIONS = new ArrayList<>(Arrays.asList(
      FieldValueMapping.builder()
                       .fieldName("fruits.tropical[]")
                       .fieldType("string-array")
                       .valueLength(0)
                       .required(true)
                       .isAncestorRequired(true)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("vegetables.trees[:]")
                       .fieldType("string-map")
                       .valueLength(0)
                       .required(true)
                       .isAncestorRequired(true)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("birds[][].nameBird")
                       .fieldType("string")
                       .valueLength(0)
                       .required(false)
                       .isAncestorRequired(true)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("animals[:][:].nameAnimal")
                       .fieldType("string")
                       .valueLength(0)
                       .required(false)
                       .isAncestorRequired(true)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build()
  ));

  public static final List<FieldValueMapping> SIMPLE_SCHEMA_REQUIRED = new ArrayList<FieldValueMapping>(Arrays.asList(
      FieldValueMapping.builder()
                       .fieldName("firstName")
                       .fieldType("string")
                       .valueLength(0)
                       .fieldValueList("Diana")
                       .required(true)
                       .isAncestorRequired(true)
                       .constraints(Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0"))
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("lastName")
                       .fieldType("string")
                       .valueLength(0)
                       .fieldValueList("García")
                       .required(true)
                       .constraints(Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0"))
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("age")
                       .fieldType("int")
                       .valueLength(0)
                       .fieldValueList("2")
                       .required(true)
                       .constraints(Collections.emptyMap())
                       .build()
  ));

  public static final String SIMPLE_SCHEMA_REQUIRED_EXPECTED = "{\"firstName\": \"Diana\",\"lastName\":\"García\"  ,\"age\":2}";

  public static final List<FieldValueMapping> SCHEMA_NESTED_ITERATION = new ArrayList<>(Arrays.asList(
      FieldValueMapping.builder()
                       .fieldName("flowers[:]")
                       .fieldType("string-map")
                       .valueLength(0)
                       .required(true)
                       .isAncestorRequired(true)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("flowers[:].name[]")
                       .fieldType("string-array")
                       .valueLength(0)
                       .required(true)
                       .isAncestorRequired(true)
                       .fieldValueList("Edelweiss")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("bush[:]")
                       .fieldType("string-map")
                       .valueLength(0)
                       .required(true)
                       .isAncestorRequired(true)
                       .fieldValueList("null")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("bush[:].maxHeight[2][:]")
                       .fieldType("int-map-array")
                       .valueLength(4)
                       .required(true)
                       .isAncestorRequired(true)
                       .fieldValueList("asdf:14")
                       .constraints(Collections.emptyMap())
                       .build(),
      FieldValueMapping.builder()
                       .fieldName("bush[:].leafs[][3:][]")
                       .fieldType("string-array-map-array")
                       .valueLength(8)
                       .required(true)
                       .isAncestorRequired(true)
                       .fieldValueList("oval")
                       .constraints(Collections.emptyMap())
                       .build()
  ));

  protected JsonSchemaFixturesConstants() {
  }
}
