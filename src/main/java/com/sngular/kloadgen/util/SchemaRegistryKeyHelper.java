/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.util;

public final class SchemaRegistryKeyHelper {

  public static final String SCHEMA_REGISTRY_DEFAULT_GROUP = "default";

  public static final String SCHEMA_REGISTRY_NAME = "schema.registry.name";

  public static final String SCHEMA_REGISTRY_CONFLUENT = "CONFLUENT";

  public static final String SCHEMA_REGISTRY_APICURIO = "APICURIO";

  public static final String SCHEMA_REGISTRY_USERNAME_KEY = "schema.registry.username";

  public static final String SCHEMA_REGISTRY_USERNAME_DEFAULT = "<username>";

  public static final String SCHEMA_REGISTRY_PASSWORD_KEY = "schema.registry.password";

  public static final String SCHEMA_REGISTRY_PASSWORD_DEFAULT = "<password>";

  public static final String SCHEMA_REGISTRY_URL_KEY = "SCHEMA_REGISTRY_URL_KEY";

  public static final String SCHEMA_REGISTRY_URL_VALUE = "SCHEMA_REGISTRY_URL_VALUE";

  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

  public static final String SCHEMA_REGISTRY_NAME_DEFAULT = "Confluent";

  public static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";

  public static final String SCHEMA_REGISTRY_AUTH_KEY = "schema.registry.auth.method";

  public static final String SCHEMA_REGISTRY_AUTH_BASIC_TYPE = "BASIC";

  public static final String SCHEMA_REGISTRY_AUTH_BEARER_TYPE = "BEARER";

  public static final String SCHEMA_REGISTRY_AUTH_SSL_TYPE = "SSL";

  public static final String SCHEMA_REGISTRY_AUTH_BEARER_DEFAULT = "<bearer>";

  public static final String SCHEMA_REGISTRY_SUBJECTS = "schema.registry.subjects";

  public static final String SCHEMA_REGISTRY_AUTH_BEARER_KEY = "schema.registry.bearer";

  public static final String SCHEMA_REGISTRY_AUTH_FLAG = "schema.registry.auth.enabled";

  public static final String ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG = "auto.register.schemas";

  public static final String SCHEMA_REGISTRY_SSL_KEYSTORE_LOCATION_KEY = "schema.registry.ssl.keystore.location";

  public static final String SCHEMA_REGISTRY_SSL_KEYSTORE_PASSWORD_KEY = "schema.registry.ssl.keystore.password";

  public static final String SCHEMA_REGISTRY_SSL_TRUSTSTORE_LOCATION_KEY = "schema.registry.ssl.truststore.location";

  public static final String SCHEMA_REGISTRY_SSL_TRUSTSTORE_PASSWORD_KEY = "schema.registry.ssl.truststore.password";

  public static final String SSL_KEYSTORE_LOCATION_KEY = "ssl.keystore.location";

  public static final String SSL_KEYSTORE_PASSWORD_KEY = "ssl.keystore.password";

  public static final String SSL_TRUSTSTORE_LOCATION_KEY = "ssl.truststore.location";

  public static final String SSL_TRUSTSTORE_PASSWORD_KEY = "ssl.truststore.password";

  private SchemaRegistryKeyHelper() {
  }
}
