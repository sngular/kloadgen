package com.sngular.kloadgen.extractor.extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.common.SchemaTypeEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.sampler.schemaregistry.schema.ApicurioParsedSchema;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ExtractorFactory {
  private final AvroExtractor avroExtractor;

  private final JsonExtractor jsonExtractor;

  private final ProtoBufExtractor protoBufExtractor;

  public ExtractorFactory() {
    this.avroExtractor = new AvroExtractor();
    this.jsonExtractor = new JsonExtractor();
    this.protoBufExtractor = new ProtoBufExtractor();
  }

  public ExtractorFactory(final AvroExtractor avroExtractor, final JsonExtractor jsonExtractor, final ProtoBufExtractor protoBufExtractor) {
    this.avroExtractor = avroExtractor;
    this.jsonExtractor = jsonExtractor;
    this.protoBufExtractor = protoBufExtractor;
  }

  public Extractor getExtractor(final String schemaType) {
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
  public Pair<String, List<FieldValueMapping>> flatPropertiesList(final String registryName, final Object schemaParsed) {
    String schemaType = null;
    List<FieldValueMapping> attributeList = new ArrayList<>();
    if(Objects.nonNull(registryName)) {
      switch (SchemaRegistryEnum.valueOf(registryName.toUpperCase())) {
        case APICURIO:
          final ApicurioParsedSchema apicurioParsedSchema = (ApicurioParsedSchema) schemaParsed;
          final Object schema = apicurioParsedSchema.getSchema();

          schemaType = apicurioParsedSchema.getType();
          attributeList = this.getExtractor(schemaType).processApicurioParsedSchema(schema);
          break;
        case CONFLUENT:
          final ParsedSchema confluentParsedSchema = (ParsedSchema) schemaParsed;

          schemaType = confluentParsedSchema.schemaType();
          attributeList = this.getExtractor(schemaType).processConfluentParsedSchema(confluentParsedSchema);
          break;
      }
    }
    return Pair.of(schemaType, attributeList);
  }
}
