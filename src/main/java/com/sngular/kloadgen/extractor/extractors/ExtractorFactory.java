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
import com.sngular.kloadgen.extractor.extractors.protobuff.ProtobuffExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ApicurioParsedSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ParsedSchemaAdapter;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.threads.JMeterContextService;

public final class ExtractorFactory {
  private static final AvroExtractor avroExtractor = new AvroExtractor();

  private static final JsonExtractor jsonExtractor = new JsonExtractor();

  private static final ProtobuffExtractor protobuffExtractor = new ProtobuffExtractor();

  private ExtractorFactory() {
  }

  public static ExtractorRegistry getExtractor(final String schemaType, final String schemaRegistryEnum) {

    if (schemaType != null && EnumUtils.isValidEnum(SchemaTypeEnum.class, schemaType.toUpperCase())) {
      final ExtractorRegistry response = switch (SchemaTypeEnum.valueOf(schemaType.toUpperCase())) {
        case JSON -> jsonExtractor;
        case AVRO -> avroExtractor;
        case PROTOBUF -> protobuffExtractor;
        default -> throw new KLoadGenException(String.format("Schema type not supported %s", schemaType));
      };
      return response;
    } else {
      throw new KLoadGenException(String.format("Schema type not supported %s", schemaType));
    }
  }

  public static SchemaRegistryEnum getSchemaRegistry(final String schemaRegistryEnum) {
    if (schemaRegistryEnum != null && EnumUtils.isValidEnum(SchemaRegistryEnum.class, schemaRegistryEnum.toUpperCase())) {
      return SchemaRegistryEnum.valueOf(schemaRegistryEnum.toUpperCase());
    } else {
      throw new KLoadGenException(String.format("Schema Registry type not supported %s", schemaRegistryEnum));
    }
  }

  public static Pair<String, List<FieldValueMapping>> flatPropertiesList(final String subjectName) {
    final Properties properties = JMeterContextService.getContext().getProperties();
    final var schemaParsed = JMeterHelper.getParsedSchema(subjectName, properties);
    final String registryName = properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
    final ParsedSchemaAdapter parsedSchemaAdapter = schemaParsed.getParsedSchemaAdapter();
    final String schemaType = parsedSchemaAdapter.getType();

    List<FieldValueMapping> attributeList = new ArrayList<>();
    SchemaRegistryEnum schemaRegistryEnum = SchemaRegistryEnum.valueOf(registryName.toUpperCase());

    Object schema = null;
    if (Objects.nonNull(registryName)) {
      //TODO change parser
      schema = switch (schemaRegistryEnum) {
        case APICURIO -> ((ApicurioParsedSchemaMetadata) parsedSchemaAdapter).getSchema();
        case CONFLUENT -> parsedSchemaAdapter.getRawSchema();
        default -> throw new KLoadGenException("Schema Registry Type nos supported " + registryName.toUpperCase());
      };
      attributeList = getExtractor(schemaType, registryName.toUpperCase()).processSchema(schema, schemaRegistryEnum);
    }
    return Pair.of(schemaType, attributeList);
  }
}