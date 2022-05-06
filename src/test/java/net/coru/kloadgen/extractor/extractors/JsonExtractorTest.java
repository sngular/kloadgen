package net.coru.kloadgen.extractor.extractors;

import static net.coru.kloadgen.model.ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MAXIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MINIMUM_VALUE;
import static net.coru.kloadgen.model.ConstraintTypeEnum.MULTIPLE_OF;
import static net.coru.kloadgen.model.ConstraintTypeEnum.REGEX;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.testutil.FileHelper;
import org.junit.jupiter.api.Test;

class JsonExtractorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final SchemaExtractor schemaExtractor = new SchemaExtractorImpl();

  private final Map<ConstraintTypeEnum, String> constraintsEmpty = new EnumMap<>(ConstraintTypeEnum.class);

  @Test
  void testBasic() throws Exception {
    File testFile = fileHelper.getFile("/jsonschema/basic.jcs");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));
    Map<ConstraintTypeEnum, String> constraints = getConstraintsMinValueMaxValue("0", "0");

    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("firstName", "string", 0, "", constraints, false, false),
            new FieldValueMapping("lastName", "string", 0, "", constraints, true, false),
            new FieldValueMapping("age", "number", 0, "", constraintsEmpty, true, false)
        );
  }

  @Test
  void testBasicArray() throws IOException {
    File testFile = fileHelper.getFile("/jsonschema/basic-array.jcs");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));
    Map<ConstraintTypeEnum, String> constraints = getConstraintsMinValueMaxValue("0", "0");

    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("fruits[]", "string-array", 0, "", constraintsEmpty, true, false),
            new FieldValueMapping("vegetables[].veggieName", "string", 0, "", constraints, true, true),
            new FieldValueMapping("vegetables[].veggieLike", "boolean", 0, "", constraintsEmpty, true, true)
        );
  }

  @Test
  void testBasicNumber() throws IOException {
    File testFile = fileHelper.getFile("/jsonschema/basic-number.jcs");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));

    assertThat(fieldValueMappingList)
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("latitude", "number", 0, "", getConstraintsBasicNumber("-90", "90", "0", "0", "0"), true, false),
            new FieldValueMapping("longitude", "number", 0, "", getConstraintsBasicNumber("-180", "180", "0", "0", "0"), true, false)
        );
  }

  @Test
  void testCollections() throws IOException {
    File testFile = fileHelper.getFile("/jsonschema/collections.jcs");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));
    Map<ConstraintTypeEnum, String> constraintsBasic = getConstraintsMinValueMaxValue("0", "0");

    assertThat(fieldValueMappingList)
        .hasSize(12)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("mapOfStrings[:]", "string-map", 0, "", constraintsEmpty, true, false),
            new FieldValueMapping("arrayOfObjectsOfBasicTypes[].stringOfObject", "string", 0, "", constraintsBasic, false, true),
            new FieldValueMapping("arrayOfObjectsOfBasicTypes[].numberOfObject", "number", 0, "", constraintsEmpty, false, true),
            new FieldValueMapping("objectOfCollectionsOfBasicTypes.arrayOfStrings[]", "string-array", 0, "", constraintsEmpty, true, true),
            new FieldValueMapping("objectOfCollectionsOfBasicTypes.mapOfIntegers[:]", "number-map", 0, "", constraintsEmpty, true, true),
            new FieldValueMapping("objectOfCollectionsOfBasicTypes.stringControl", "string",0, "", constraintsBasic, false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.stringControl", "string", 0, "", constraintsBasic, false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.arrayOfObjectsPerson[].namePerson", "string", 0, "", constraintsBasic, false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.arrayOfObjectsPerson[].phonePerson", "number", 0, "", constraintsEmpty, false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].nameDog", "string", 0, "", constraintsBasic, false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.dogId", "number", 0, "", constraintsEmpty, false, true),
            new FieldValueMapping("objectOfCollectionsOfObject.mapOfObjectsDog[:].vetData.breedName", "string", 0, "", constraintsBasic, false, true)
        );
  }

  @Test
  void testComplexDefinitions() throws IOException{
    File testFile = fileHelper.getFile("/jsonschema/complex-definitions.jcs");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));
    Map<ConstraintTypeEnum, String> constraints = getConstraintsMinValueMaxValue("0", "0");

    assertThat(fieldValueMappingList)
        .hasSize(8)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("objectOfDefinitions.stringControl", "string", 0, "", constraints, true, true),
            new FieldValueMapping("objectOfDefinitions.arrayOfStrings[]", "string-array", 0, "", constraintsEmpty, true, true),
            new FieldValueMapping("objectOfDefinitions.mapOfStrings[:]", "string-map", 0, "", constraintsEmpty, true, true),
            new FieldValueMapping("arrayOfObjects[].stringOfObject", "string", 0, "", constraints, false, true),
            new FieldValueMapping("arrayOfObjects[].numberOfObject", "number", 0, "", constraintsEmpty, true, true),
            new FieldValueMapping("mapOfObjects[:].arrayOfInternalObject[]", "string-array", 0, "", constraintsEmpty, true, true),
            new FieldValueMapping("mapOfMaps[:][:].stringControlObject", "string", 0, "", constraints, true, true),
            new FieldValueMapping("mapOfMaps[:][:].arrayOfArraysOfStrings[][]", "string-array-array", 0, "", constraintsEmpty, false, true)
        );
  }

  @Test
  void testComplexDocument() throws IOException {
    File testFile = fileHelper.getFile("/jsonschema/complex-document.jcs");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "JSON"));
    Map<ConstraintTypeEnum, String> constraints = getConstraintsMinValueMaxValue("0", "0");

    assertThat(fieldValueMappingList)
        .hasSize(40)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("_id", "string", 0, "", constraints, true, false),
            new FieldValueMapping("userId", "number", 0, "", getConstraintsBasicNumber("1", "0", "0", "0", "0"), true, false),
            new FieldValueMapping("storeId", "number", 0, "", getConstraintsBasicNumber("0", "0", "0", "0", "0"), true, false),
            new FieldValueMapping("snapshotId", "string", 0, "", constraints, true, false),
            new FieldValueMapping("addressId", "string",  0, "", constraints, true, false),
            new FieldValueMapping("addressLine", "string", 0, "", constraints, true, false),
            new FieldValueMapping("alias", "string", 0, "", constraints, true, false),
            new FieldValueMapping("contactInformation.email", "string", 0, "", constraints, true, true),
            new FieldValueMapping("contactInformation.firstName", "string", 0, "", constraints, true, true),
            new FieldValueMapping("contactInformation.middleName", "string", 0, "", constraints, false, true),
            new FieldValueMapping("contactInformation.lastName", "string", 0, "", constraints, false, true),
            new FieldValueMapping("contactInformation.honorific", "string", 0, "", getConstraintsMinValueMaxValueRegex("2", "3", "^[a-zA-Z]{2,3}$"), false, true),
            new FieldValueMapping("contactInformation.phones[].prefix", "string", 0, "", getConstraintsMinValueMaxValue("2", "3"), true, true),
            new FieldValueMapping("contactInformation.phones[].number", "string", 0, "", getConstraintsMinValueMaxValue("0", "6"), true, true),
            new FieldValueMapping("countryCode", "string", 0, "", getConstraintsMinValueMaxValueRegex("2", "2","^[a-zA-Z]{2}$"), true, false),
            new FieldValueMapping("location.streetName", "string", 0, "", constraints, false, false),
            new FieldValueMapping("location.streetNumber", "string", 0, "", constraints, false, false),
            new FieldValueMapping("location.floor", "string", 0, "", constraints, false, false),
            new FieldValueMapping("location.door", "string", 0, "", constraints, false, false),
            new FieldValueMapping("location.doorCode", "string", 0, "", constraints, false, false),
            new FieldValueMapping("location.zipCode", "string", 0, "", constraints, false, false),
            new FieldValueMapping("geopoliticalSubdivisions.level1.code", "string", 0, "", getConstraintsMinValueMaxValue("2", "3"), false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level1.freeForm", "string", 0, "", getConstraintsMinValueMaxValue("1", "256"), false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level2.code", "string", 0, "", getConstraintsMinValueMaxValue("2", "3"), false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level2.freeForm", "string", 0, "", getConstraintsMinValueMaxValue("1", "256"), false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level3.code", "string", 0, "", getConstraintsMinValueMaxValue("2", "3"), false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level3.freeForm", "string", 0, "", getConstraintsMinValueMaxValue("1", "256"), false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level4.code", "string", 0, "", getConstraintsMinValueMaxValue("2", "3"), false, true),
            new FieldValueMapping("geopoliticalSubdivisions.level4.freeForm", "string", 0, "", getConstraintsMinValueMaxValue("1", "256"), false, true),
            new FieldValueMapping("_metadata.createdAt", "timestamp", 0, "", constraintsEmpty, true, true),
            new FieldValueMapping("_metadata.createdBy", "string", 0, "", constraints, true, true),
            new FieldValueMapping("_metadata.lastUpdatedAt", "timestamp", 0, "", constraintsEmpty, true, true),
            new FieldValueMapping("_metadata.lastUpdatedBy", "string", 0, "", constraints, true, true),
            new FieldValueMapping("_metadata.deletedAt", "timestamp", 0, "", constraintsEmpty, false, true),
            new FieldValueMapping("_metadata.projectVersion", "string", 0, "", constraints, true, true),
            new FieldValueMapping("_metadata.projectName", "string", 0, "", constraints, true, true),
            new FieldValueMapping("_metadata.deletedBy", "string", 0, "", constraints, false, true),
            new FieldValueMapping("_metadata.schema", "number", 0, "", getConstraintsBasicNumber("0", "0", "0", "0", "0"), true, true),
            new FieldValueMapping("_entity", "enum", 0, "AddressSnapshot", constraintsEmpty, true, false),
            new FieldValueMapping("_class", "enum", 0, "AddressSnapshot", constraintsEmpty, true, false)
        );
  }


  private Map<ConstraintTypeEnum, String> getConstraintsMinValueMaxValue(String minValue, String maxValue){
    Map<ConstraintTypeEnum, String> constraints = new EnumMap<>(ConstraintTypeEnum.class);
    constraints.put(MINIMUM_VALUE, minValue);
    constraints.put(MAXIMUM_VALUE, maxValue);
    return constraints;
  }

  private Map<ConstraintTypeEnum, String> getConstraintsBasicNumber(String minValue, String maxValue, String excludedMinValue, String excludedMaxValue, String multipleOf){
    Map<ConstraintTypeEnum, String> constraints = new EnumMap<>(ConstraintTypeEnum.class);
    constraints.put(MINIMUM_VALUE, minValue);
    constraints.put(MAXIMUM_VALUE, maxValue);
    constraints.put(EXCLUDED_MINIMUM_VALUE, excludedMinValue);
    constraints.put(EXCLUDED_MAXIMUM_VALUE, excludedMaxValue);
    constraints.put(MULTIPLE_OF, multipleOf);
    return constraints;
  }

  private Map<ConstraintTypeEnum, String> getConstraintsMinValueMaxValueRegex(String minValue, String maxValue, String regex){
    Map<ConstraintTypeEnum, String> constraints = new EnumMap<>(ConstraintTypeEnum.class);
    constraints.put(MINIMUM_VALUE, minValue);
    constraints.put(MAXIMUM_VALUE, maxValue);
    constraints.put(REGEX, regex);
    return constraints;
  }


}
