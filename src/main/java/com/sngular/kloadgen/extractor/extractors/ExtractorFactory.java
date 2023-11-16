package com.sngular.kloadgen.extractor.extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.extractors.avro.AvroExtractor;
import com.sngular.kloadgen.extractor.extractors.json.JsonExtractor;
import com.sngular.kloadgen.extractor.extractors.protobuff.ProtobuffExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.schemaregistry.adapter.impl.AbstractParsedSchemaAdapter;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ApicurioAbstractParsedSchemaMetadata;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.threads.JMeterContextService;

public final class ExtractorFactory {
  private static final AvroExtractor AVRO_EXTRACTOR = new AvroExtractor();

  private static final JsonExtractor JSON_EXTRACTOR = new JsonExtractor();

  private static final ProtobuffExtractor PROTOBUFF_EXTRACTOR = new ProtobuffExtractor();

  private ExtractorFactory() {
  }

  public static <T> ExtractorRegistry<T> getExtractor(final String schemaType) {

    if (schemaType != null && EnumUtils.isValidEnum(SchemaTypeEnum.class, schemaType.toUpperCase())) {
      final ExtractorRegistry<T> response = switch (SchemaTypeEnum.valueOf(schemaType.toUpperCase())) {
        case JSON -> AVRO_EXTRACTOR;
        case AVRO -> JSON_EXTRACTOR;
        case PROTOBUF -> PROTOBUFF_EXTRACTOR;
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
    final AbstractParsedSchemaAdapter parsedSchemaAdapter = schemaParsed.getParsedSchemaAdapter();
    final String schemaType = parsedSchemaAdapter.getType();

    final List<FieldValueMapping> attributeList = new ArrayList<>();
    final SchemaRegistryEnum schemaRegistryEnum = SchemaRegistryEnum.valueOf(registryName.toUpperCase());

    final Object schema;
    if (Objects.nonNull(registryName)) {
      //TODO change parser
      schema = switch (schemaRegistryEnum) {
        case APICURIO -> ((ApicurioAbstractParsedSchemaMetadata) parsedSchemaAdapter).getSchema();
        case CONFLUENT -> parsedSchemaAdapter.getRawSchema();
        default -> throw new KLoadGenException("Schema Registry Type nos supported " + registryName.toUpperCase());
      };
      attributeList = getExtractor(schemaType, registryName.toUpperCase()).processSchema(schema, schemaRegistryEnum);
    }
    return Pair.of(schemaType, attributeList);
  }
}