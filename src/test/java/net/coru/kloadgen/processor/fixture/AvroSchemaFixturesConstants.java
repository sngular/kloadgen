package net.coru.kloadgen.processor.fixture;

import static java.util.Arrays.asList;

import java.util.List;

import net.coru.kloadgen.model.FieldValueMapping;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class AvroSchemaFixturesConstants {

  public static final Schema BASIC_STRING_ARRAY_MAP_NULL = SchemaBuilder.builder()
                                                                        .record("array")
                                                                        .fields()
                                                                        .name("simple_int")
                                                                        .type()
                                                                        .intType()
                                                                        .noDefault()
                                                                        .name("simple_int_null")
                                                                        .type()
                                                                        .optional()
                                                                        .intType()
                                                                        .endRecord();

  public static final List<FieldValueMapping> BASIC_STRING_ARRAY_MAP_NULL_FIELDS = asList(
      new FieldValueMapping("simple_int", "int", 0, "", true, true),
      new FieldValueMapping("simple_int_null", "int", 0, "null", false, false)
  );

  public static final Schema ARRAY_OPTIONAL_NULL = SchemaBuilder.builder()
                                                                .record("array")
                                                                .fields()
                                                                .name("record_array")
                                                                .type()
                                                                .nullable()
                                                                .array()
                                                                .items()
                                                                .type(SchemaBuilder.record("record").fields().name("null_field").type().optional().stringType()
                                                                                   .name("not_null_field").type().optional().intType().endRecord())
                                                                .noDefault()
                                                                .name("array_null")
                                                                .type()
                                                                .nullable()
                                                                .array()
                                                                .items()
                                                                .type(SchemaBuilder.record("null_record").fields().name("string_null_field").type().optional().stringType()
                                                                                   .name("int_null_field").type().optional().intType().endRecord())
                                                                .noDefault()
                                                                .endRecord();

  public static final List<FieldValueMapping> ARRAY_OPTIONAL_NULL_FIELDS = asList(
      new FieldValueMapping("record_array[1].null_field", "string", 0, "null", false, true),
      new FieldValueMapping("record_array[1].not_null_field", "int", 0, "null", false, true),
      new FieldValueMapping("array_null[1].string_null_field", "string", 0, "null", false, false),
      new FieldValueMapping("array_null[1].int_null_field", "int", 0, "null", false, false)

  );

  public static final Schema MAP_OPTIONAL_NULL = SchemaBuilder.builder()
                                                              .record("maps")
                                                              .fields()
                                                              .name("record_map")
                                                              .type()
                                                              .nullable()
                                                              .map()
                                                              .values(SchemaBuilder.record("record").fields().name("null_field").type().optional().stringType()
                                                                                   .name("not_null_field").type().optional().intType().endRecord())
                                                              .noDefault()
                                                              .name("map_null")
                                                              .type()
                                                              .nullable()
                                                              .map()
                                                              .values(SchemaBuilder.record("null_record").fields().name("string_null_field").type().optional().stringType()
                                                                                   .name("int_null_field").type().optional().intType().endRecord())
                                                              .noDefault()
                                                              .endRecord();

  public static final List<FieldValueMapping> MAP_OPTIONAL_NULL_FIELDS = asList(
      new FieldValueMapping("record_map[1:].null_field", "string", 0, "null", false, true),
      new FieldValueMapping("record_map[1:].not_null_field", "int", 0, "null", false, true),
      new FieldValueMapping("map_null[1:].string_null_field", "string", 0, "null", false, false),
      new FieldValueMapping("map_null[1:].int_null_field", "int", 0, "null", false, false)
  );
}
