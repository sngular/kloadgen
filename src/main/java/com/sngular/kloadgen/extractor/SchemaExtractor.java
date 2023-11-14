package com.sngular.kloadgen.extractor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.extractor.extractors.ExtractorFactory;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
public final class SchemaExtractor {

  private SchemaRegistryEnum type;

  public SchemaExtractor() {
  }

  public void setType(final SchemaRegistryEnum type) {
    this.type = type;
  }

  public static Pair<String, List<FieldValueMapping>> flatPropertiesList(final String subjectName) {
    return ExtractorFactory.flatPropertiesList(subjectName);
  }

  public static List<FieldValueMapping> flatPropertiesList(final ParsedSchema parserSchema) {
    return ExtractorFactory.getExtractor(parserSchema.schemaType()).processSchema(parserSchema, SchemaRegistryEnum.CONFLUENT);
  }

  public static String readSchemaFile(final String filePath) throws IOException {
    return readLineByLine(filePath);
  }

  private static String readLineByLine(final String filePath) throws IOException {
    final StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }

}
