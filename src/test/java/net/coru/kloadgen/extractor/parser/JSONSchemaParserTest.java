package net.coru.kloadgen.extractor.parser;

import static net.coru.kloadgen.extractor.parser.fixture.JsonSchemaFixturesConstants.COLLECTIONS_SCHEMA;
import static net.coru.kloadgen.extractor.parser.fixture.JsonSchemaFixturesConstants.COMPLEX_SCHEMA;
import static net.coru.kloadgen.extractor.parser.fixture.JsonSchemaFixturesConstants.DEFINITIONS_COMPLEX_SCHEMA;
import static net.coru.kloadgen.extractor.parser.fixture.JsonSchemaFixturesConstants.MEDIUM_COMPLEX_SCHEMA;
import static net.coru.kloadgen.extractor.parser.fixture.JsonSchemaFixturesConstants.NESTED_COLLECTIONS_SCHEMA;
import static net.coru.kloadgen.extractor.parser.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA;
import static net.coru.kloadgen.extractor.parser.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_ARRAY;
import static net.coru.kloadgen.extractor.parser.fixture.JsonSchemaFixturesConstants.SIMPLE_SCHEMA_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Stream;
import net.coru.kloadgen.extractor.parser.impl.JSONSchemaParser;
import net.coru.kloadgen.model.json.Field;
import net.coru.kloadgen.model.json.NumberField;
import net.coru.kloadgen.model.json.Schema;
import net.coru.kloadgen.model.json.StringField;
import net.coru.kloadgen.model.json.UUIDField;
import net.coru.kloadgen.testutil.FileHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JSONSchemaParserTest {

  private static final FileHelper resourceAsFile = new FileHelper();

  private static final JSONSchemaParser jsonSchemaParser = new JSONSchemaParser();

  private static Stream<Arguments> parametersForShouldParseJSONSchemaDocument() throws Exception {
    return Stream.of(
        Arguments.of(resourceAsFile.getContent("/jsonschema/basic.jcs"), SIMPLE_SCHEMA),
        Arguments.of(resourceAsFile.getContent("/jsonschema/basic-number.jcs"), SIMPLE_SCHEMA_NUMBER),
        Arguments.of(resourceAsFile.getContent("/jsonschema/basic-array.jcs"), SIMPLE_SCHEMA_ARRAY),
        Arguments.of(resourceAsFile.getContent("/jsonschema/complex-document.jcs"), COMPLEX_SCHEMA),
        Arguments.of(resourceAsFile.getContent("/jsonschema/medium-document.jcs"), MEDIUM_COMPLEX_SCHEMA),
        Arguments.of(resourceAsFile.getContent("/jsonschema/collections.jcs"), COLLECTIONS_SCHEMA),
        Arguments.of(resourceAsFile.getContent("/jsonschema/nested-collections.jcs"), NESTED_COLLECTIONS_SCHEMA),
        Arguments.of(resourceAsFile.getContent("/jsonschema/complex-definitions.jcs"), DEFINITIONS_COMPLEX_SCHEMA)
    );
  }

  @ParameterizedTest
  @MethodSource("parametersForShouldParseJSONSchemaDocument")
  void shouldParseJSONSchemaDocument(String schemaAsJson, Schema expected) {

    Schema result = jsonSchemaParser.parse(schemaAsJson);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void shouldParseJSONSchemaDocument() throws Exception {

    Schema result = jsonSchemaParser.parse(resourceAsFile.getContent("/jsonschema/multiple-type.jcs"));

    assertThat(result)
            .extracting(Schema::getProperties)
            .satisfies(this::multiTypeTestStringOrNumber);
  }

  private boolean multiTypeTestStringOrNumber(Object field) {
    Set<String> propertyNames = Set.of("id", "version", "dtype", "timestamp", "event_type");
    return (field instanceof StringField || field instanceof NumberField || field instanceof UUIDField) &&
        propertyNames.contains(((Field)field).getName());
  }

}