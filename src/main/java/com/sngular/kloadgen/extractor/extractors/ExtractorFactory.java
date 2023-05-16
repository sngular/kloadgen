package com.sngular.kloadgen.extractor.extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.extractors.protobuff.ProtoBufConfluentExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ApicurioParsedSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.ParsedSchemaAdapter;
import com.sngular.kloadgen.util.JMeterHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.threads.JMeterContextService;

public final class ExtractorFactory {
  private static AvroExtractor avroExtractor = new AvroExtractor();

  private static JsonExtractor jsonExtractor = new JsonExtractor();

  private static ProtoBufConfluentExtractor protoBufConfluentExtractor = new ProtoBufConfluentExtractor();

  private ExtractorFactory() {
  }

  public static Extractor getExtractor(final String schemaType) {
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
          response = protoBufConfluentExtractor;
          break;
        default:
          throw new KLoadGenException(String.format("Schema type not supported %s", schemaType));
      }
      return response;
    } else {
      throw new KLoadGenException(String.format("Schema type not supported %s", schemaType));
    }
  }

  public static Pair<String, List<FieldValueMapping>> flatPropertiesList(final String subjectName) {
    final Properties properties = JMeterContextService.getContext().getProperties();

    final var schemaParsed = JMeterHelper.getParsedSchema(subjectName, properties);
    final String registryName = properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
    String schemaType = null;
    final ParsedSchemaAdapter parsedSchemaAdapter = schemaParsed.getParsedSchemaAdapter();
    schemaType = parsedSchemaAdapter.getType();

    final List<FieldValueMapping> attributeList = new ArrayList<>();
    if (Objects.nonNull(registryName)) {
      switch (SchemaRegistryEnum.valueOf(registryName.toUpperCase())) {
        case APICURIO:
          getExtractor(schemaType).processSchema(((ApicurioParsedSchemaMetadata) parsedSchemaAdapter).getSchema());
          break;
        case CONFLUENT:
          getExtractor(schemaType).processSchema(parsedSchemaAdapter.getRawSchema());
          break;
        default:
          throw new KLoadGenException("Schema Registry Type nos supported " + registryName.toUpperCase());
      }
    }
    return Pair.of(schemaType, attributeList);
  }
}
