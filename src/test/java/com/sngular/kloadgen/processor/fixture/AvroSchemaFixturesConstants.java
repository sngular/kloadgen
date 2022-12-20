package com.sngular.kloadgen.processor.fixture;

import java.util.Arrays;
import java.util.List;

import com.sngular.kloadgen.model.FieldValueMapping;
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

  public static final List<FieldValueMapping> BASIC_STRING_ARRAY_MAP_NULL_FIELDS = Arrays.asList(
      FieldValueMapping.builder().fieldName("simple_int").fieldType("int").valueLength(0).fieldValueList("").required(true).isAncestorRequired(true).build(),
      FieldValueMapping.builder().fieldName("simple_int_null").fieldType("int").valueLength(0).fieldValueList("null").required(false).isAncestorRequired(false).build()
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

  public static final List<FieldValueMapping> ARRAY_OPTIONAL_NULL_FIELDS = Arrays.asList(
      FieldValueMapping.builder().fieldName("record_array[1].null_field").fieldType("string").valueLength(0).fieldValueList("null").required(false).isAncestorRequired(true)
                       .build(),
      FieldValueMapping.builder().fieldName("record_array[1].not_null_field").fieldType("int").valueLength(0).fieldValueList("null").required(false).isAncestorRequired(true)
                       .build(),
      FieldValueMapping.builder().fieldName("array_null[1].string_null_field").fieldType("string").valueLength(0).fieldValueList("null").required(false).isAncestorRequired(false)
                       .build(),
      FieldValueMapping.builder().fieldName("array_null[1].int_null_field").fieldType("int").valueLength(0).fieldValueList("null").required(false).isAncestorRequired(false).build()
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

  public static final List<FieldValueMapping> MAP_OPTIONAL_NULL_FIELDS = Arrays.asList(
      FieldValueMapping.builder().fieldName("record_map[1:].null_field").fieldType("string").valueLength(0).fieldValueList("null").required(false).isAncestorRequired(true).build(),
      FieldValueMapping.builder().fieldName("record_map[1:].not_null_field").fieldType("int").valueLength(0).fieldValueList("null").required(false).isAncestorRequired(true)
                       .build(),
      FieldValueMapping.builder().fieldName("map_null[1:].string_null_field").fieldType("string").valueLength(0).fieldValueList("null").required(false).isAncestorRequired(false)
                       .build(),
      FieldValueMapping.builder().fieldName("map_null[1:].int_null_field").fieldType("int").valueLength(0).fieldValueList("null").required(false).isAncestorRequired(false).build()
  );

  protected AvroSchemaFixturesConstants() {
  }
}