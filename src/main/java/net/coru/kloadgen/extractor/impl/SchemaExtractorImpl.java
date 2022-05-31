/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.extractor.impl;

import static net.coru.kloadgen.common.SchemaTypeEnum.AVRO;
import static net.coru.kloadgen.common.SchemaTypeEnum.JSON;
import static net.coru.kloadgen.common.SchemaTypeEnum.PROTOBUF;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
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

  private AvroExtractor avroExtractor = new AvroExtractor();

  private JsonExtractor jsonExtractor = new JsonExtractor();

  private ProtoBufExtractor protoBufExtractor = new ProtoBufExtractor();

  private JMeterHelper jMeterHelper = new JMeterHelper();

  @Override
  public Pair<String, List<FieldValueMapping>> flatPropertiesList(String subjectName) throws IOException, RestClientException {
    ParsedSchema schema = jMeterHelper.getParsedSchema(subjectName, JMeterContextService.getContext().getProperties());
    List<FieldValueMapping> attributeList = new ArrayList<>();
    if (AVRO.name().equalsIgnoreCase(schema.schemaType())) {
      (((AvroSchema) schema).rawSchema()).getFields().forEach(field -> avroExtractor.processField(field, attributeList, true, false));
    } else if (JSON.name().equalsIgnoreCase(schema.schemaType())) {
      attributeList.addAll(jsonExtractor.processSchema(((JsonSchema) schema).toJsonNode()));
    } else if (PROTOBUF.name().equalsIgnoreCase(schema.schemaType())) {
      com.squareup.wire.schema.internal.parser.ProtoFileElement protoFileElement = (((ProtobufSchema) schema).rawSchema());
      protoFileElement.getTypes().forEach(field -> protoBufExtractor.processField(field, attributeList, protoFileElement.getImports(), false));
    } else {
      throw new KLoadGenException(String.format("Schema type not supported %s", schema.schemaType()));
    }
    return Pair.of(schema.schemaType(), attributeList);
  }

  @Override
  public List<FieldValueMapping> flatPropertiesList(ParsedSchema parserSchema) {
    return processSchema(parserSchema);
  }

  @Override
  public ParsedSchema schemaTypesList(File schemaFile, String schemaType) throws IOException {
    ParsedSchema parsedSchema;
    if ("AVRO".equalsIgnoreCase(schemaType)) {
      parsedSchema = avroExtractor.getParsedSchema(readLineByLine(schemaFile.getPath()));
    } else if ("JSON".equalsIgnoreCase(schemaType)) {
      parsedSchema = new JsonSchema(readLineByLine(schemaFile.getPath()));
    } else {
      parsedSchema = new ProtobufSchema(readLineByLine(schemaFile.getPath()));
    }
    return parsedSchema;
  }

  private static String readLineByLine(String filePath) throws IOException {
    StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }

  private List<FieldValueMapping> processSchema(ParsedSchema schema) {
    if ("AVRO".equalsIgnoreCase(schema.schemaType())) {
      return avroExtractor.processSchema(((AvroSchema) schema).rawSchema());
    } else if ("JSON".equalsIgnoreCase(schema.schemaType())) {
      return jsonExtractor.processSchema(((JsonSchema) schema).toJsonNode());
    } else if ("PROTOBUF".equalsIgnoreCase(schema.schemaType())) {
      return protoBufExtractor.processSchema(((ProtobufSchema) schema).rawSchema());
    } else {
      throw new KLoadGenException("Unsupported Schema Type");
    }
  }


}
