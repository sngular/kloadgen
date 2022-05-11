package net.coru.kloadgen.extractor.extractors;

import static net.coru.kloadgen.model.ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MAXIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MINIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MULTIPLE_OF;
import static net.coru.kloadgen.model.ConstraintTypeEnum.REGEX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
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

    Map<ConstraintTypeEnum, String> constraints = new HashMap<>();
    constraints.put(MINIMUM_VALUE, "0");
    constraints.put(MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("firstName", "string", 0, "", constraints, false, false),
            new FieldValueMapping("lastName", "string", 0, "", constraints, true, false),
            new FieldValueMapping("age", "number", 0, "", true, false)
        );
  }

  @Test
  @DisplayName("Should extract a basic array")
  void testBasicArray() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/basic-array.jcs");

    Map<ConstraintTypeEnum, String> constraints = new HashMap<>();
    constraints.put(MINIMUM_VALUE, "0");
    constraints.put(MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("fruits[]", "string-array", 0, "", true, false),
            new FieldValueMapping("vegetables[].veggieName", "string", 0, "", constraints, true, true),
            new FieldValueMapping("vegetables[].veggieLike", "boolean", 0, "",true, true)
        );
  }

  @Test
  @DisplayName("Should extract basic number type")
  void testBasicNumber() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/basic-number.jcs");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("latitude", "number", 0, "", new HashMap<ConstraintTypeEnum, String>() {{
              put(MINIMUM_VALUE, "-90");
              put(MAXIMUM_VALUE, "90");
              put(EXCLUDED_MINIMUM_VALUE, "0");
              put(EXCLUDED_MAXIMUM_VALUE, "0");
              put(MULTIPLE_OF, "0");
            }}, true, false),
            new FieldValueMapping("longitude", "number", 0, "", new HashMap<ConstraintTypeEnum, String>() {{
              put(MINIMUM_VALUE, "-180");
              put(MAXIMUM_VALUE, "180");
              put(EXCLUDED_MINIMUM_VALUE, "0");
              put(EXCLUDED_MAXIMUM_VALUE, "0");
              put(MULTIPLE_OF, "0");
            }}, true, false)
        );
  }

  @Test
  @DisplayName("Should extract optional collections and optional collections inside objects")
  void testFlatPropertiesOptionalCollections() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/collections.jcs");

    Map<ConstraintTypeEnum, String> constraints = new HashMap<>();
    constraints.put(MINIMUM_VALUE, "0");
    constraints.put(MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(12)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("mapOfStrings[:]", "string-map", 0, "", true, false),
            new FieldValueMapping("arrayOfObjectsOfBasicTypes[].stringOfObject", "string", 0, "", constraints,false, true),
            new FieldValueMapping("arrayOfObjectsOfBasicTypes[].numberOfObject", "number", 0, "", false, true),
            new FieldValueMapping("objectOfCollectionsOfBasicTypes.arrayOfStrings[]", "string-array", 0, "", true, true),
            new FieldValueMapping("objectOfCollectionsOfBasicTypes.mapOfIntegers[:]", "number-map", 0, "", true, true),
            new FieldValueMapping("objectOfCollectionsOfBasicTypes.stringControl", "string", 0, "", constraints,false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.stringControl", "string", 0, "", constraints, false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.arrayOfObjectsPerson[].namePerson", "string",0, "", constraints,false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.arrayOfObjectsPerson[].phonePerson", "number", 0, "", false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].nameDog", "string", 0, "", constraints, false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.dogId", "number", 0, "", false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.breedName", "string", 0, "", constraints, false, true)
        );
  }

  @Test
  @DisplayName("Should extract maps, arrays or objects of other maps, arrays or objects")
  void testComplexDefinitions() throws Exception{
    String testFile = fileHelper.getContent("/jsonschema/complex-definitions.jcs");

    Map<ConstraintTypeEnum, String> constraints = new HashMap<>();
    constraints.put(MINIMUM_VALUE, "0");
    constraints.put(MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(8)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("objectOfDefinitions.stringControl", "string", 0, "", constraints, true, true),
            new FieldValueMapping("objectOfDefinitions.arrayOfStrings[]", "string-array", 0, "", true, true),
            new FieldValueMapping("objectOfDefinitions.mapOfStrings[:]", "string-map", 0, "", true, true),
            new FieldValueMapping("arrayOfObjects[].stringOfObject", "string", 0, "", constraints, false, true),
            new FieldValueMapping("arrayOfObjects[].numberOfObject", "number", 0, "", true, true),
            new FieldValueMapping("mapOfObjects[:].arrayOfInternalObject[]", "string-array", 0, "", true, true),
            new FieldValueMapping("mapOfMaps[:][:].stringControlObject", "string", 0, "", constraints, true, true),
            new FieldValueMapping("mapOfMaps[:][:].arrayOfArraysOfStrings[][]", "string-array-array", 0, "", false, true)
        );
  }

  @Test
  @DisplayName("Should propagate required status to children fields not required of a required field")
  void testRequiredPropagationChildrenFields() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/complex-document.jcs");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .contains(
            new FieldValueMapping("geopoliticalSubdivisions.level1.code", "string", 0, "", new HashMap<ConstraintTypeEnum, String>() {{
              put(MINIMUM_VALUE, "2");
              put(MAXIMUM_VALUE, "3");
            }}, false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level1.freeForm", "string", 0, "", new HashMap<ConstraintTypeEnum, String>() {{
              put(MINIMUM_VALUE, "1");
              put(MAXIMUM_VALUE, "256");
            }}, false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level2.code", "string", 0, "", new HashMap<ConstraintTypeEnum, String>() {{
              put(MINIMUM_VALUE, "2");
              put(MAXIMUM_VALUE, "3");
            }}, false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level2.freeForm", "string", 0, "", new HashMap<ConstraintTypeEnum, String>() {{
              put(MINIMUM_VALUE, "1");
              put(MAXIMUM_VALUE, "256");
            }}, false, true)
        );
  }

  @Test
  @DisplayName("Should extract fields in definitions in Json Schema")
  void testShouldExtractJsonSchemaDefinitions() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/medium-document.jcs");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList).contains(
        new FieldValueMapping("duty.amount.value", "number", 0, "", false, false),
        new FieldValueMapping("duty.amount.currency", "string", 0, "", new HashMap<ConstraintTypeEnum, String>() {{
          put(MINIMUM_VALUE, "0");
          put(MAXIMUM_VALUE, "0");
          put(REGEX, "^(.*)$");
        }}, false, false),
        new FieldValueMapping("duty.amount.exponent", "number", 0, "", false, false));
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

    Map<ConstraintTypeEnum, String> constraints = new HashMap<>();
    constraints.put(MINIMUM_VALUE, "0");
    constraints.put(MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList)
        .hasSize(8)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("arrayOfMapsOfObjects[][:].stringObject", "string", 0, "", constraints,false, true),
            new FieldValueMapping("arrayOfMapsOfObjects[][:].numberObject", "number", 0, "", false, true),
            new FieldValueMapping("arrayOfArraysOfStrings[][]", "string-array-array", 0, "", false, true),
            new FieldValueMapping("mapOfArraysOfStrings[:][]", "string-array-map", 0, "", false, true),
            new FieldValueMapping("mapOfMapsOfObjects[:][:].name4Object", "string", 0, "", constraints,false, true),
            new FieldValueMapping("mapOfMapsOfObjects[:][:].number4Object", "number", 0, "", false, true),
            new FieldValueMapping("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].stringControl","string", 0, "", constraints, false, true),
            new FieldValueMapping("mapOfObjectsOfCollections[:].arrayOfMapsOfObject[][:].numberControl", "number", 0, "", false, true)
        );
  }

  @Test
  @DisplayName("Should extract maps of simple data-types from JsonSchema")
  void testShouldExtractMapSimpleDataType() throws Exception {
    String testFile = fileHelper.getContent("/jsonschema/test-map.jcs");

    Map<ConstraintTypeEnum, String> constraints = new HashMap<>();
    constraints.put(MINIMUM_VALUE, "0");
    constraints.put(MAXIMUM_VALUE, "0");

    List<FieldValueMapping> fieldValueMappingList = jsonExtractor.processSchema(new JsonSchema(testFile).toJsonNode());

    assertThat(fieldValueMappingList).contains(
        new FieldValueMapping("firstName", "string", 0, "", constraints, false, false),
        new FieldValueMapping("lastName", "string", 0, "", constraints, true, false),
        new FieldValueMapping("age", "number", 0, "", true, false),
        new FieldValueMapping("testMap.itemType[:]", "number-map", 0, "", true, true),
        new FieldValueMapping("testMap.itemTipo[:]", "string-map", 0, "", true, true)

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
