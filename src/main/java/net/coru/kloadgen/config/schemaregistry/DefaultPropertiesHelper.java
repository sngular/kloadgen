package net.coru.kloadgen.config.schemaregistry;

import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_NO;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_BEARER_DEFAULT;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_BEARER_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_PASSWORD_DEFAULT;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_USERNAME_DEFAULT;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_USERNAME_KEY;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.coru.kloadgen.model.PropertyMapping;

class DefaultPropertiesHelper {

  static final List<PropertyMapping> DEFAULTS = ImmutableList.of(
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_AUTH_FLAG).propertyValue(FLAG_NO).build(),
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_AUTH_KEY).propertyValue(SCHEMA_REGISTRY_AUTH_BASIC_TYPE).build(),
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_USERNAME_KEY).propertyValue(SCHEMA_REGISTRY_USERNAME_DEFAULT).build(),
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_PASSWORD_KEY).propertyValue(SCHEMA_REGISTRY_PASSWORD_DEFAULT).build(),
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_AUTH_BEARER_KEY).propertyValue(SCHEMA_REGISTRY_AUTH_BEARER_DEFAULT).build());
}
