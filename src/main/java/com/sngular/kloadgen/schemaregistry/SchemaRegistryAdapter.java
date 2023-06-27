package com.sngular.kloadgen.schemaregistry;

import java.util.Collection;
import java.util.Map;

import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.schemaregistry.adapter.impl.SchemaMetadataAdapter;

public interface SchemaRegistryAdapter {

  String getSchemaRegistryUrlKey();

  void setSchemaRegistryClient(String url, Map<String, ?> properties);

  void setSchemaRegistryClient(Map<String, ?> properties);

  Collection<String> getAllSubjects();

  BaseSchemaMetadata getLatestSchemaMetadata(String subjectName);

  BaseParsedSchema getSchemaBySubject(String subjectName);

  BaseParsedSchema getSchemaBySubjectAndId(String subjectName, BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata);
}
