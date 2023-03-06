/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.SchemaExtractor;
import com.sngular.kloadgen.extractor.extractors.AvroExtractor;
import com.sngular.kloadgen.extractor.extractors.Extractor;
import com.sngular.kloadgen.extractor.extractors.JsonExtractor;
import com.sngular.kloadgen.extractor.extractors.ProtoBufExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.sampler.schemaregistry.schema.ApicurioParsedSchema;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.commons.lang3.EnumUtils;
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

  private Extractor getExtractor(final String schemaType) {
    if (schemaType != null && EnumUtils.isValidEnum(SchemaTypeEnum.class, schemaType.toUpperCase())) {
      final Extractor response;
      switch (SchemaTypeEnum.valueOf(schemaType.toUpperCase())) {
        case JSON:
          response = jsonExtractor;
          break;
        case AVRO:
          response = avroExtractor;
          break;
        case PROTOBUF:
          response = protoBufExtractor;
          break;
        default:
          throw new KLoadGenException(String.format("Schema type not supported %s", schemaType));
      }
      return response;
    } else {
      throw new KLoadGenException(String.format("Schema type not supported %s", schemaType));
    }
  }

  @Override
  public final Pair<String, List<FieldValueMapping>> flatPropertiesList(final String subjectName) throws IOException, RestClientException {
    String schemaType = null;
    final Properties properties = JMeterContextService.getContext().getProperties();

    final var schemaParsed = JMeterHelper.getParsedSchema(subjectName, properties);
    List<FieldValueMapping> attributeList = new ArrayList<>();

    final String registryName = properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
    if (Objects.nonNull(registryName) && registryName.equalsIgnoreCase(SchemaRegistryEnum.APICURIO.toString())) {
      final ApicurioParsedSchema apicurioParsedSchema = (ApicurioParsedSchema) schemaParsed;
      final Object schema = apicurioParsedSchema.getSchema();

      schemaType = apicurioParsedSchema.getType();
      attributeList = getExtractor(schemaType).processApicurioParsedSchema(schema);
    } else if (Objects.nonNull(registryName) && registryName.equalsIgnoreCase(SchemaRegistryEnum.CONFLUENT.toString())) {
      final ParsedSchema confluentParsedSchema = (ParsedSchema) schemaParsed;

      schemaType = confluentParsedSchema.schemaType();
      attributeList = getExtractor(schemaType).processConfluentParsedSchema(confluentParsedSchema);
    }
    return Pair.of(schemaType, attributeList);
  }

  @Override
  public final List<FieldValueMapping> flatPropertiesList(final ParsedSchema parserSchema) {
    return processSchema(parserSchema);
  }

  @Override
  public final ParsedSchema schemaTypesList(final File schemaFile, final String schemaType) throws IOException {
    return getExtractor(schemaType).getParsedSchema(readLineByLine(schemaFile.getPath()));
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
    result.addAll(getExtractor(schema.schemaType()).processSchema(schema));
    return result;
  }

}
