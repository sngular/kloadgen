package com.sngular.kloadgen.extractor.extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.extractors.avro.AvroExtractor;
import com.sngular.kloadgen.extractor.extractors.json.JsonExtractor;
import com.sngular.kloadgen.extractor.extractors.protobuf.ProtobufExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.ParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.AbstractParsedSchemaAdapter;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ApicurioAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.commons.lang3.EnumUtils;
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

  public static ExtractorRegistry getExtractor(final String schemaType) {

    if (schemaType != null && EnumUtils.isValidEnum(SchemaTypeEnum.class, schemaType.toUpperCase())) {
      final ExtractorRegistry response = switch (SchemaTypeEnum.valueOf(schemaType.toUpperCase())) {
        case JSON -> jsonExtractor;
        case AVRO -> avroExtractor;
        case PROTOBUF -> protobufExtractor;
        default -> throw new KLoadGenException(String.format("Schema type not supported %s", schemaType));
      };
      return response;
    } else {
      throw new KLoadGenException(String.format("Schema type not supported %s", schemaType));
    }
  }

  public static Pair<String, List<FieldValueMapping>> flatPropertiesList(final String subjectName) {
    final Properties properties = JMeterContextService.getContext().getProperties();
    final var schemaParsed = JMeterHelper.getParsedSchema(subjectName, properties);
    final String registryName = properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
    final AbstractParsedSchemaAdapter abstractParsedSchemaAdapter = schemaParsed.getParsedSchemaAdapter();
    final String schemaType = abstractParsedSchemaAdapter.getType();

    final List<FieldValueMapping> attributeList = new ArrayList<>();
    final SchemaRegistryEnum schemaRegistryEnum = SchemaRegistryEnum.valueOf(registryName.toUpperCase());

    final Object schema;
    if (Objects.nonNull(registryName)) {
      //TODO change parser
      schema = switch (schemaRegistryEnum) {
        case APICURIO -> ((ApicurioAbstractParsedSchemaMetadata) abstractParsedSchemaAdapter).getSchema();
        case CONFLUENT -> abstractParsedSchemaAdapter.getRawSchema();
      };
      attributeList.addAll(getExtractor(schemaType).processSchema(new ParsedSchema(schema), schemaRegistryEnum));
    }
    return Pair.of(schemaType, attributeList);
  }
}