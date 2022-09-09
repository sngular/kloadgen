package net.coru.kloadgen.extractor.parser;

import java.util.Set;
import java.util.stream.Stream;

import net.coru.kloadgen.extractor.parser.fixture.JsonSchemaFixturesConstants;
import net.coru.kloadgen.extractor.parser.impl.JSONSchemaParser;
import net.coru.kloadgen.model.json.Field;
import net.coru.kloadgen.model.json.NumberField;
import net.coru.kloadgen.model.json.Schema;
import net.coru.kloadgen.model.json.StringField;
import net.coru.kloadgen.model.json.UUIDField;
import net.coru.kloadgen.testutil.FileHelper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JSONSchemaParserTest {

  private static final FileHelper FILE_HELPER = new FileHelper();

  private static final JSONSchemaParser SCHEMA_PARSER = new JSONSchemaParser();

  private static Stream<Arguments> parametersForShouldParseJSONSchemaDocument() throws Exception {
    return Stream.of(
        Arguments.of(FILE_HELPER.getContent("/jsonschema/basic.jcs"), JsonSchemaFixturesConstants.SIMPLE_SCHEMA),
        Arguments.of(FILE_HELPER.getContent("/jsonschema/basic-number.jcs"), JsonSchemaFixturesConstants.SIMPLE_SCHEMA_NUMBER),
        Arguments.of(FILE_HELPER.getContent("/jsonschema/basic-array.jcs"), JsonSchemaFixturesConstants.SIMPLE_SCHEMA_ARRAY),
        Arguments.of(FILE_HELPER.getContent("/jsonschema/complex-document.jcs"), JsonSchemaFixturesConstants.COMPLEX_SCHEMA),
        Arguments.of(FILE_HELPER.getContent("/jsonschema/medium-document.jcs"), JsonSchemaFixturesConstants.MEDIUM_COMPLEX_SCHEMA),
        Arguments.of(FILE_HELPER.getContent("/jsonschema/collections.jcs"), JsonSchemaFixturesConstants.COLLECTIONS_SCHEMA),
        Arguments.of(FILE_HELPER.getContent("/jsonschema/nested-collections.jcs"), JsonSchemaFixturesConstants.NESTED_COLLECTIONS_SCHEMA),
        Arguments.of(FILE_HELPER.getContent("/jsonschema/complex-definitions.jcs"), JsonSchemaFixturesConstants.DEFINITIONS_COMPLEX_SCHEMA)
    );
  }

  @ParameterizedTest
  @MethodSource("parametersForShouldParseJSONSchemaDocument")
  void shouldParseJSONSchemaDocument(final String schemaAsJson, final Schema expected) {

    final Schema result = SCHEMA_PARSER.parse(schemaAsJson);

    Assertions.assertThat(result).isEqualTo(expected);
  }

  @Test
  void shouldParseJSONSchemaDocument() throws Exception {

    final Schema result = SCHEMA_PARSER.parse(FILE_HELPER.getContent("/jsonschema/multiple-type.jcs"));

    Assertions.assertThat(result)
              .extracting(Schema::getProperties)
              .satisfies(this::multiTypeTestStringOrNumber);
  }

  private boolean multiTypeTestStringOrNumber(final Object field) {
    final Set<String> propertyNames = Set.of("id", "version", "dtype", "timestamp", "event_type");
    return (field instanceof StringField || field instanceof NumberField || field instanceof UUIDField)
           && propertyNames.contains(((Field) field).getName());
  }

}