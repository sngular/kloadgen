/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.schemaregistry;

import static java.util.Arrays.asList;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_NO;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_DEFAULT;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_BEARER_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_DEFAULT;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_DEFAULT;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY;

import java.util.List;
import net.coru.kloadgen.model.PropertyMapping;

class DefaultPropertiesHelper {

  protected static final List<PropertyMapping> DEFAULTS = asList(
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_AUTH_FLAG).propertyValue(FLAG_NO).build(),
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_AUTH_KEY).propertyValue(SCHEMA_REGISTRY_AUTH_BASIC_TYPE).build(),
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_USERNAME_KEY).propertyValue(SCHEMA_REGISTRY_USERNAME_DEFAULT).build(),
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_PASSWORD_KEY).propertyValue(SCHEMA_REGISTRY_PASSWORD_DEFAULT).build(),
      PropertyMapping.builder().propertyName(SCHEMA_REGISTRY_AUTH_BEARER_KEY).propertyValue(SCHEMA_REGISTRY_AUTH_BEARER_DEFAULT).build());
}
