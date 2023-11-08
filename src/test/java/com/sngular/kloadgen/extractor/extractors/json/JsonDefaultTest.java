package com.sngular.kloadgen.extractor.extractors.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.testutil.FileHelper;
import org.assertj.core.api.Assertions;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonDefaultTest {

  private final FileHelper fileHelper = new FileHelper();

  private final Extractor<String> jsonDefaultExtractor = new JsonDefaultExtractor();

  @Test
  @DisplayName("Should extract basic types")
  void testBasic() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/basic.jcs");

    final Map<ConstraintTypeEnum, String> constraints = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0");
    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(3)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("firstName").fieldType("string").constraints(constraints).required(false).isAncestorRequired(false).build(),
                  FieldValueMapping.builder().fieldName("lastName").fieldType("string").constraints(constraints).required(true).isAncestorRequired(false).build(),
                  FieldValueMapping.builder().fieldName("age").fieldType("number").required(true).isAncestorRequired(false).build());
  }

  @Test
  @DisplayName("Should extract a basic array")
  void testBasicArray() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/basic-array.jcs");

    final Map<ConstraintTypeEnum, String> constraints = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0");
    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(3)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("fruits[]").fieldType("string-array").required(true).isAncestorRequired(false).build(),
                  FieldValueMapping.builder().fieldName("vegetables[].veggieName").fieldType("string").constraints(constraints).required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("vegetables[].veggieLike").fieldType("boolean").required(true).isAncestorRequired(true).build());
  }

  @Test
  @DisplayName("Should extract basic number type")
  void testBasicNumber() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/basic-number.jcs");

    final Map<ConstraintTypeEnum, String> constraintsLatitude = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "-90", ConstraintTypeEnum.MAXIMUM_VALUE, "90",
                                                                       ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE, "0", ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE, "0",
                                                                       ConstraintTypeEnum.MULTIPLE_OF, "0");

    final Map<ConstraintTypeEnum, String> constraintsLongitude = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "-180", ConstraintTypeEnum.MAXIMUM_VALUE, "180",
                                                                        ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE, "0", ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE, "0",
                                                                        ConstraintTypeEnum.MULTIPLE_OF, "0");

    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(2)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("latitude").fieldType("number").constraints(constraintsLatitude).required(true).isAncestorRequired(false).build(),
                  FieldValueMapping.builder().fieldName("longitude").fieldType("number").constraints(constraintsLongitude).required(true).isAncestorRequired(false).build());
  }

  @Test
  @DisplayName("Should extract optional collections and optional collections inside objects")
  void testFlatPropertiesOptionalCollections() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/collections.jcs");

    final Map<ConstraintTypeEnum, String> constraints = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0");

    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(12)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("mapOfStrings[:]").fieldType("string-map").required(true).isAncestorRequired(false).build(),
                  FieldValueMapping.builder().fieldName("arrayOfObjectsOfBasicTypes[].stringOfObject").fieldType("string").constraints(constraints).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("arrayOfObjectsOfBasicTypes[].numberOfObject").fieldType("number").required(false).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("objectOfCollectionsOfBasicTypes.arrayOfStrings[]").fieldType("string-array").required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("objectOfCollectionsOfBasicTypes.mapOfIntegers[:]").fieldType("number-map").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("objectOfCollectionsOfBasicTypes.stringControl").fieldType("string").constraints(constraints).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.stringControl").fieldType("string").constraints(constraints).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.arrayOfObjectsPerson[].namePerson").fieldType("string").constraints(constraints)
                                   .required(false).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.arrayOfObjectsPerson[].phonePerson").fieldType("number").required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.mapOfObjectsDog[:].nameDog").fieldType("string").constraints(constraints).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.dogId").fieldType("number").required(false).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.breedName").fieldType("string").constraints(constraints)
                                   .required(false).isAncestorRequired(true).build());
  }

  @Test
  @DisplayName("Should extract maps, arrays or objects of other maps, arrays or objects")
  void testComplexDefinitions() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/complex-definitions.jcs");

    final Map<ConstraintTypeEnum, String> constraints = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0");

    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(8)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("objectOfDefinitions.stringControl").fieldType("string").constraints(constraints).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("objectOfDefinitions.arrayOfStrings[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("objectOfDefinitions.mapOfStrings[:]").fieldType("string-map").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("arrayOfObjects[].stringOfObject").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("arrayOfObjects[].numberOfObject").fieldType("number").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("mapOfObjects[:].arrayOfInternalObject[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("mapOfMaps[:][:].stringControlObject").fieldType("string").constraints(constraints).required(true).isAncestorRequired(true)
                                   .build(),
                  FieldValueMapping.builder().fieldName("mapOfMaps[:][:].arrayOfArraysOfStrings[][]").fieldType("string-array-array").required(false).isAncestorRequired(true)
                                   .build());
  }

  @Test
  @DisplayName("Should propagate required status to children fields not required of a required field")
  void testRequiredPropagationChildrenFields() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/complex-document.jcs");

    final Map<ConstraintTypeEnum, String> constraintsCode = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "2", ConstraintTypeEnum.MAXIMUM_VALUE, "3");
    final Map<ConstraintTypeEnum, String> constraintsFreeForm = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "1", ConstraintTypeEnum.MAXIMUM_VALUE, "256");

    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList)
              .contains(
                  FieldValueMapping.builder().fieldName("geopoliticalSubdivisions.level1.code").fieldType("string").constraints(constraintsCode).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("geopoliticalSubdivisions.level1.freeForm").fieldType("string").constraints(constraintsFreeForm).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("geopoliticalSubdivisions.level2.code").fieldType("string").constraints(constraintsCode).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("geopoliticalSubdivisions.level2.freeForm").fieldType("string").constraints(constraintsFreeForm).required(false)
                                   .isAncestorRequired(true).build());
  }

  @Test
  @DisplayName("Should extract fields in definitions in Json Schema")
  void testShouldExtractJsonSchemaDefinitions() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/medium-document.jcs");

    final Map<ConstraintTypeEnum, String> constraints = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0", ConstraintTypeEnum.REGEX, "^(.*)$");

    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList)
              .contains(
                  FieldValueMapping.builder().fieldName("duty.amount.value").fieldType("number").required(false).isAncestorRequired(false).build(),
                  FieldValueMapping.builder().fieldName("duty.amount.currency").fieldType("string").constraints(constraints).required(false).isAncestorRequired(false).build(),
                  FieldValueMapping.builder().fieldName("duty.amount.exponent").fieldType("number").required(false).isAncestorRequired(false).build());
  }

  @Test
  @DisplayName("Should choose one or another type")
  void testMultipleType() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/multiple-type-single.jcs");

    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(1)
              .satisfiesExactly(
                  fieldValueMapping -> Set.of("number", "uuid").contains(fieldValueMapping.getFieldType()));
  }

  @Test
  @DisplayName("Should extract optional nested-collections and optional nested-collections inside objects")
  void testFlatPropertiesOptionalNestedCollections() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/nested-collections.jcs");

    final Map<ConstraintTypeEnum, String> constraints = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0");

    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(8)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("arrayOfMapsOfObjects[][:].stringObject").fieldType("string").constraints(constraints).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("arrayOfMapsOfObjects[][:].numberObject").fieldType("number").required(false).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("arrayOfArraysOfStrings[][]").fieldType("string-array-array").required(false).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("mapOfArraysOfStrings[:][]").fieldType("string-array-map").required(false).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("mapOfMapsOfObjects[:][:].name4Object").fieldType("string").constraints(constraints).required(false)
                                   .isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("mapOfMapsOfObjects[:][:].number4Object").fieldType("number").required(false).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].stringControl").fieldType("string").constraints(constraints)
                                   .required(false).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].numberControl").fieldType("number").required(false)
                                   .isAncestorRequired(true).build());
  }

  @Test
  @DisplayName("Should extract maps of simple data-types from JsonSchema")
  void testShouldExtractMapSimpleDataType() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/test-map.jcs");

    final Map<ConstraintTypeEnum, String> constraints = Map.of(ConstraintTypeEnum.MINIMUM_VALUE, "0", ConstraintTypeEnum.MAXIMUM_VALUE, "0");

    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);
    final List<FieldValueMapping> fieldValueMappingList = jsonDefaultExtractor.processSchema(schema.toString());

    Assertions.assertThat(fieldValueMappingList).contains(
        FieldValueMapping.builder().fieldName("firstName").fieldType("string").constraints(constraints).required(false).isAncestorRequired(false).build(),
        FieldValueMapping.builder().fieldName("lastName").fieldType("string").constraints(constraints).required(true).isAncestorRequired(false).build(),
        FieldValueMapping.builder().fieldName("age").fieldType("number").required(true).isAncestorRequired(false).build(),
        FieldValueMapping.builder().fieldName("testMap.itemType[:]").fieldType("number-map").required(true).isAncestorRequired(true).build(),
        FieldValueMapping.builder().fieldName("testMap.itemTipo[:]").fieldType("string-map").required(true).isAncestorRequired(true).build()
    );
  }

  @Test
  @DisplayName("Should capture 3+ level exception in collections. Three levels of nested collections are not allowed")
  void testFlatPropertiesCaptureThreeLevelException() throws Exception {
    final String testFile = fileHelper.getContent("/jsonschema/test-level-nested-exception.jcs");
    final JSONObject jsonObject = new JSONObject(testFile);
    final Schema schema = SchemaLoader.load(jsonObject);

    Assertions.assertThatExceptionOfType(KLoadGenException.class)
              .isThrownBy(() -> jsonDefaultExtractor.processSchema(schema.toString()))
              .withMessage("Wrong Json Schema, 3+ consecutive nested collections are not allowed");
  }

}