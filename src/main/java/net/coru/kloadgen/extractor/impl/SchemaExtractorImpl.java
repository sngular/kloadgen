/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.extractor.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import com.squareup.wire.schema.internal.parser.TypeElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import net.coru.kloadgen.common.SchemaTypeEnum;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.extractor.extractors.AvroExtractor;
import net.coru.kloadgen.extractor.extractors.JsonExtractor;
import net.coru.kloadgen.extractor.extractors.ProtoBufExtractor;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.JMeterHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.threads.JMeterContextService;

public class SchemaExtractorImpl implements SchemaExtractor {

  private final AvroExtractor avroExtractor;

  private final JsonExtractor jsonExtractor;

  private final ProtoBufExtractor protoBufExtractor;

  public SchemaExtractorImpl() {
    this.avroExtractor = new AvroExtractor();
    this.jsonExtractor = new JsonExtractor();
    this.protoBufExtractor = new ProtoBufExtractor();
  }

  public SchemaExtractorImpl(final AvroExtractor avroExtractor, final JsonExtractor jsonExtractor, final ProtoBufExtractor protoBufExtractor) {
    this.avroExtractor = avroExtractor;
    this.jsonExtractor = jsonExtractor;
    this.protoBufExtractor = protoBufExtractor;
  }

  @Override
  public final Pair<String, List<FieldValueMapping>> flatPropertiesList(final String subjectName) throws IOException, RestClientException {
    final var schema = JMeterHelper.getParsedSchema(subjectName, JMeterContextService.getContext().getProperties());
    final List<FieldValueMapping> attributeList = new ArrayList<>();
    if (SchemaTypeEnum.AVRO.name().equalsIgnoreCase(schema.schemaType())) {
      (((AvroSchema) schema).rawSchema()).getFields().forEach(field -> avroExtractor.processField(field, attributeList, true, false));
    } else if (SchemaTypeEnum.JSON.name().equalsIgnoreCase(schema.schemaType())) {
      attributeList.addAll(jsonExtractor.processSchema(((JsonSchema) schema).toJsonNode()));
    } else if (SchemaTypeEnum.PROTOBUF.name().equalsIgnoreCase(schema.schemaType())) {
      final var protoFileElement = ((ProtobufSchema) schema).rawSchema();
      final HashMap<String, TypeElement> nestedTypes = new HashMap<>();
      protoFileElement.getTypes().forEach(field -> protoBufExtractor.processField(field, attributeList, protoFileElement.getImports(), false, nestedTypes));
    } else {
      throw new KLoadGenException(String.format("Schema type not supported %s", schema.schemaType()));
    }
    return Pair.of(schema.schemaType(), attributeList);
  }

  @Override
  public final List<FieldValueMapping> flatPropertiesList(final ParsedSchema parserSchema) {
    return processSchema(parserSchema);
  }

  @Override
  public final ParsedSchema schemaTypesList(final File schemaFile, final String schemaType) throws IOException {
    final ParsedSchema parsedSchema;
    if ("AVRO".equalsIgnoreCase(schemaType)) {
      parsedSchema = avroExtractor.getParsedSchema(readLineByLine(schemaFile.getPath()));
    } else if ("JSON".equalsIgnoreCase(schemaType)) {
      parsedSchema = new JsonSchema(readLineByLine(schemaFile.getPath()));
    } else {
      parsedSchema = new ProtobufSchema(readLineByLine(schemaFile.getPath()));
    }
    return parsedSchema;
  }

  private static String readLineByLine(final String filePath) throws IOException {
    final StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }

  private List<FieldValueMapping> processSchema(final ParsedSchema schema) {
    final var result = new ArrayList<FieldValueMapping>();
    if ("AVRO".equalsIgnoreCase(schema.schemaType())) {
      result.addAll(avroExtractor.processSchema(((AvroSchema) schema).rawSchema()));
    } else if ("JSON".equalsIgnoreCase(schema.schemaType())) {
      result.addAll(jsonExtractor.processSchema(((JsonSchema) schema).toJsonNode()));
    } else if ("PROTOBUF".equalsIgnoreCase(schema.schemaType())) {
      result.addAll(protoBufExtractor.processSchema(((ProtobufSchema) schema).rawSchema()));
    } else {
      throw new KLoadGenException("Unsupported Schema Type");
    }
    return result;
  }

}
