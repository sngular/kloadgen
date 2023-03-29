package com.sngular.kloadgen.sampler.schemaregistry;

import java.util.Collection;
import java.util.Map;

import com.sngular.kloadgen.sampler.schemaregistry.adapter.impl.BaseSchemaMetadata;
import com.sngular.kloadgen.sampler.schemaregistry.adapter.impl.SchemaMetadataAdapter;

public interface SchemaRegistryAdapter {

  String getSchemaRegistryUrlKey();

  void setSchemaRegistryClient(String url, Map<String, ?> properties);

  void setSchemaRegistryClient(Map<String, ?> properties);

  Collection<String> getAllSubjects();

  BaseSchemaMetadata getLatestSchemaMetadata(String subjectName);

  Object getSchemaBySubject(String subjectName);

  Object getSchemaBySubjectAndId(String subjectName, BaseSchemaMetadata<? extends SchemaMetadataAdapter> metadata);
}
