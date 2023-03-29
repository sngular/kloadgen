package com.sngular.kloadgen.sampler.schemaregistry;

import java.util.Collection;
import java.util.Map;

public interface SchemaRegistryManager {

  String getSchemaRegistryUrlKey();

  void setSchemaRegistryClient(String url, Map<String, ?> properties);

  void setSchemaRegistryClient(Map<String, ?> properties);

  Map<String, String> getPropertiesMap();

  Collection<String> getAllSubjects();

  Object getLatestSchemaMetadata(String subjectName);

  Object getSchemaBySubject(String subjectName);

  Object getSchemaBySubjectAndId(String subjectName, Object metadata);
}
