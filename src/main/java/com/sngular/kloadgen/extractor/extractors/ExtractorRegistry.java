package com.sngular.kloadgen.extractor.extractors;

import java.util.List;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.parsedschema.AbstractParsedSchema;

public interface ExtractorRegistry<T extends AbstractParsedSchema<?>> {

  List<FieldValueMapping> processSchema(final AbstractParsedSchema<?> schema, SchemaRegistryEnum registry);

  T processSchema(final String fileContent);

  List<String> getSchemaNameList(final String schema, SchemaRegistryEnum registryEnum);

}
