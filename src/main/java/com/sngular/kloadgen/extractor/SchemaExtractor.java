package com.sngular.kloadgen.extractor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.ExtractorFactory;
import com.sngular.kloadgen.model.FieldValueMapping;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import org.apache.commons.lang3.tuple.Pair;

public class SchemaExtractor {

  private SchemaRegistryEnum type;

  public SchemaExtractor() {
  }

  public final SchemaRegistryEnum getType(){
    return type;
  }

  public final void setType(SchemaRegistryEnum type){
    this.type = type;
  }

  public static Pair<String, List<FieldValueMapping>> flatPropertiesList(final String subjectName) {
    return ExtractorFactory.flatPropertiesList(subjectName);
  }

  public static List<FieldValueMapping> flatPropertiesList(final ParsedSchema parserSchema) {
    return ExtractorFactory.getExtractor(parserSchema.schemaType(), "CONFLUENT").processSchema(parserSchema, SchemaRegistryEnum.CONFLUENT);
  }

  public static List<String> schemaTypesList(final File schemaFile, final String schemaType, String registry) throws IOException {
    return ExtractorFactory.getExtractor(schemaType, registry).getSchemaNameList(readLineByLine(schemaFile.getPath()),
            ExtractorFactory.getSchemaRegistry(registry));
  }

  private static String readLineByLine(final String filePath) throws IOException {
    final StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }

  private static List<FieldValueMapping> processSchema(final ParsedSchema schema) {
    return ExtractorFactory.getExtractor(schema.schemaType(),SchemaRegistryEnum.CONFLUENT.name()).processSchema(schema.rawSchema().toString(), SchemaRegistryEnum.CONFLUENT);
  }

}
