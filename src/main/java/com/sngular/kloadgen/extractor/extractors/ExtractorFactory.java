/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor.extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.extractors.avro.AvroExtractor;
import com.sngular.kloadgen.extractor.extractors.json.JsonExtractor;
import com.sngular.kloadgen.extractor.extractors.protobuf.ProtobufExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.threads.JMeterContextService;

public final class ExtractorFactory {
  private static AvroExtractor avroExtractor = new AvroExtractor();

  private static JsonExtractor jsonExtractor = new JsonExtractor();

  private static ProtobufExtractor protobufExtractor = new ProtobufExtractor();

  private ExtractorFactory() {
  }

  public static void configExtractorFactory(final AvroExtractor avroExtractor, final JsonExtractor jsonExtractor, final ProtobufExtractor protobufExtractor) {
    ExtractorFactory.avroExtractor = avroExtractor;
    ExtractorFactory.jsonExtractor = jsonExtractor;
    ExtractorFactory.protobufExtractor = protobufExtractor;
  }

  public static ExtractorRegistry<?> getExtractor(final String schemaType) {

    if (schemaType != null && EnumUtils.isValidEnum(SchemaTypeEnum.class, schemaType.toUpperCase())) {
      return switch (SchemaTypeEnum.valueOf(schemaType.toUpperCase())) {
        case JSON -> jsonExtractor;
        case AVRO -> avroExtractor;
        case PROTOBUF -> protobufExtractor;
      };
    } else {
      throw new KLoadGenException(String.format("Schema type not supported %s", schemaType));
    }
  }

  public static Pair<String, List<FieldValueMapping>> flatPropertiesList(final String subjectName) {
    final Properties properties = JMeterContextService.getContext().getProperties();
    final var schemaParsed = JMeterHelper.getParsedSchema(subjectName, properties);
    final String registryName = properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
    final String schemaType = schemaParsed.getSchemaType();

    final List<FieldValueMapping> attributeList = new ArrayList<>();
    final SchemaRegistryEnum schemaRegistryEnum = SchemaRegistryEnum.valueOf(registryName.toUpperCase());

    if (StringUtils.isNotEmpty(registryName)) {
      attributeList.addAll(getExtractor(schemaType).processSchema(schemaParsed, schemaRegistryEnum));
    }
    return Pair.of(schemaType, attributeList);
  }
}