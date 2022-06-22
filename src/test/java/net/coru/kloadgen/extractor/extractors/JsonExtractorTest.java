package net.coru.kloadgen.extractor.extractors;

import static net.coru.kloadgen.model.ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MAXIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MINIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MULTIPLE_OF;
import static net.coru.kloadgen.model.ConstraintTypeEnum.REGEX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.confluent.kafka.schemaregistry.json.JsonSchema;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.testutil.FileHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final JsonExtractor jsonExtractor = new JsonExtractor();

  @Test
  @DisplayName("Should extract basic types")
  void testBasic() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/basic.jcs");

    Map<ConstraintTypeEnum, String> constraints = Map.of(MINIMUM_VALUE, "0", MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("firstName").fieldType("string").constraints(constraints).required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("lastName").fieldType("string").constraints(constraints).required(true).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("age").fieldType("number").required(true).isAncestorRequired(false).build()
        );
  }

  @Test
  @DisplayName("Should extract a basic array")
  void testBasicArray() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/basic-array.jcs");

    Map<ConstraintTypeEnum, String> constraints = Map.of(MINIMUM_VALUE, "0", MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("fruits[]").fieldType("string-array").required(true).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("vegetables[].veggieName").fieldType("string").constraints(constraints).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("vegetables[].veggieLike").fieldType("boolean").required(true).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should extract basic number type")
  void testBasicNumber() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/basic-number.jcs");

    Map<ConstraintTypeEnum, String> constraintsLatitude = Map.of(MINIMUM_VALUE, "-90", MAXIMUM_VALUE, "90",
                                                                 EXCLUDED_MINIMUM_VALUE, "0", EXCLUDED_MAXIMUM_VALUE, "0", MULTIPLE_OF, "0");

    Map<ConstraintTypeEnum, String> constraintsLongitude = Map.of(MINIMUM_VALUE, "-180", MAXIMUM_VALUE, "180",
                                                                  EXCLUDED_MINIMUM_VALUE, "0", EXCLUDED_MAXIMUM_VALUE, "0", MULTIPLE_OF, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(2)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("latitude").fieldType("number").constraints(constraintsLatitude).required(true).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("longitude").fieldType("number").constraints(constraintsLongitude).required(true).isAncestorRequired(false).build()
        );
  }

  @Test
  @DisplayName("Should extract optional collections and optional collections inside objects")
  void testFlatPropertiesOptionalCollections() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/collections.jcs");

    Map<ConstraintTypeEnum, String> constraints = Map.of(MINIMUM_VALUE, "0", MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(12)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("mapOfStrings[:]").fieldType("string-map").required(true).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("arrayOfObjectsOfBasicTypes[].stringOfObject").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("arrayOfObjectsOfBasicTypes[].numberOfObject").fieldType("number").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfCollectionsOfBasicTypes.arrayOfStrings[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfCollectionsOfBasicTypes.mapOfIntegers[:]").fieldType("number-map").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfCollectionsOfBasicTypes.stringControl").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.stringControl").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.arrayOfObjectsPerson[].namePerson").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.arrayOfObjectsPerson[].phonePerson").fieldType("number").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.mapOfObjectsDog[:].nameDog").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.dogId").fieldType("number").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.breedName").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should extract maps, arrays or objects of other maps, arrays or objects")
  void testComplexDefinitions() throws Exception{
    String testFile = fileHelper.getContent("/jsonschema/complex-definitions.jcs");

    Map<ConstraintTypeEnum, String> constraints = Map.of(MINIMUM_VALUE, "0", MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(8)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("objectOfDefinitions.stringControl").fieldType("string").constraints(constraints).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfDefinitions.arrayOfStrings[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("objectOfDefinitions.mapOfStrings[:]").fieldType("string-map").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("arrayOfObjects[].stringOfObject").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("arrayOfObjects[].numberOfObject").fieldType("number").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mapOfObjects[:].arrayOfInternalObject[]").fieldType("string-array").required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mapOfMaps[:][:].stringControlObject").fieldType("string").constraints(constraints).required(true).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mapOfMaps[:][:].arrayOfArraysOfStrings[][]").fieldType("string-array-array").required(false).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should propagate required status to children fields not required of a required field")
  void testRequiredPropagationChildrenFields() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/complex-document.jcs");

    Map<ConstraintTypeEnum, String> constraintsCode = Map.of(MINIMUM_VALUE, "2", MAXIMUM_VALUE, "3");
    Map<ConstraintTypeEnum, String> constraintsFreeForm = Map.of(MINIMUM_VALUE, "1", MAXIMUM_VALUE, "256");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .contains(
            FieldValueMapping.builder().fieldName("geopoliticalSubdivisions.level1.code").fieldType("string").constraints(constraintsCode).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("geopoliticalSubdivisions.level1.freeForm").fieldType("string").constraints(constraintsFreeForm).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("geopoliticalSubdivisions.level2.code").fieldType("string").constraints(constraintsCode).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("geopoliticalSubdivisions.level2.freeForm").fieldType("string").constraints(constraintsFreeForm).required(false).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should extract fields in definitions in Json Schema")
  void testShouldExtractJsonSchemaDefinitions() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/medium-document.jcs");

    Map<ConstraintTypeEnum, String> constraints = Map.of(MINIMUM_VALUE, "0", MAXIMUM_VALUE, "0", REGEX, "^(.*)$");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .contains(
            FieldValueMapping.builder().fieldName("duty.amount.value").fieldType("number").required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("duty.amount.currency").fieldType("string").constraints(constraints).required(false).isAncestorRequired(false).build(),
            FieldValueMapping.builder().fieldName("duty.amount.exponent").fieldType("number").required(false).isAncestorRequired(false).build()
        );
  }

  @Test
  @DisplayName("Should choose one or another type")
  void testMultipleType() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/multiple-type-single.jcs");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(1)
        .satisfiesExactly(
            fieldValueMapping -> Set.of("number", "uuid").contains(fieldValueMapping.getFieldType())
        );
  }

  @Test
  @DisplayName("Should extract optional nested-collections and optional nested-collections inside objects")
  void testFlatPropertiesOptionalNestedCollections() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/nested-collections.jcs");

    Map<ConstraintTypeEnum, String> constraints = Map.of(MINIMUM_VALUE, "0", MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(8)
        .containsExactlyInAnyOrder(
            FieldValueMapping.builder().fieldName("arrayOfMapsOfObjects[][:].stringObject").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("arrayOfMapsOfObjects[][:].numberObject").fieldType("number").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("arrayOfArraysOfStrings[][]").fieldType("string-array-array").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mapOfArraysOfStrings[:][]").fieldType("string-array-map").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mapOfMapsOfObjects[:][:].name4Object").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mapOfMapsOfObjects[:][:].number4Object").fieldType("number").required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].stringControl").fieldType("string").constraints(constraints).required(false).isAncestorRequired(true).build(),
            FieldValueMapping.builder().fieldName("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].numberControl").fieldType("number").required(false).isAncestorRequired(true).build()
        );
  }

  @Test
  @DisplayName("Should extract maps of simple data-types from JsonSchema")
  void testShouldExtractMapSimpleDataType() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/test-map.jcs");

    Map<ConstraintTypeEnum, String> constraints = Map.of(MINIMUM_VALUE, "0", MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList).contains(
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
    String testFile = fileHelper.getContent("/jsonschema/test-level-nested-exception.jcs");
    assertThatExceptionOfType(KLoadGenException.class)
        .isThrownBy(() -> {
          List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());
          assertThat(fieldValueMappingList).isNull();
        })
        .withMessage("Wrong Json Schema, 3+ consecutive nested collections are not allowed");
  }

}