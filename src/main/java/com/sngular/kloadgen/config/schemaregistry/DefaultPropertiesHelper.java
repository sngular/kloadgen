/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.schemaregistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sngular.kloadgen.model.PropertyMapping;
import com.sngular.kloadgen.util.ProducerKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;

class DefaultPropertiesHelper {

  protected static final List<PropertyMapping> DEFAULTS = new ArrayList<>(Arrays.asList(
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG).propertyValue(ProducerKeysHelper.FLAG_NO).build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY).propertyValue(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE).build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY).propertyValue(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_DEFAULT).build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY).propertyValue(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_DEFAULT).build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_KEY).propertyValue(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_DEFAULT)
                     .build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SSL_KEYSTORE_LOCATION_KEY).propertyValue("").build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SSL_KEYSTORE_PASSWORD_KEY).propertyValue("").build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY).propertyValue("").build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY).propertyValue("").build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SSL_KEYSTORE_LOCATION_KEY).propertyValue("").build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SSL_KEYSTORE_PASSWORD_KEY).propertyValue("").build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SSL_TRUSTSTORE_LOCATION_KEY).propertyValue("").build(),
      PropertyMapping.builder().propertyName(SchemaRegistryKeyHelper.SSL_TRUSTSTORE_PASSWORD_KEY).propertyValue("").build()
  ));
  private DefaultPropertiesHelper() {
  }

}
