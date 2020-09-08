/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.util;

public class SchemaRegistryKeyHelper {

	public static final String SCHEMA_REGISTRY_USERNAME_KEY = "schema.registry.username";

	public static final String SCHEMA_REGISTRY_USERNAME_DEFAULT = "<username>";

	public static final String SCHEMA_REGISTRY_PASSWORD_KEY = "schema.registry.password";

	public static final String SCHEMA_REGISTRY_PASSWORD_DEFAULT = "<password>";

	public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";

	public static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";

	public static final String SCHEMA_REGISTRY_AUTH_KEY = "schema.registry.auth.method";

	public static final String SCHEMA_REGISTRY_AUTH_BASIC_TYPE = "BASIC";

	public static final String SCHEMA_REGISTRY_AUTH_BEARER_TYPE = "BEARER";

	public static final String SCHEMA_REGISTRY_AUTH_BEARER_DEFAULT = "<bearer>";

	public static final String SCHEMA_REGISTRY_SUBJECTS = "schema.registry.subjects";

	public static final String SCHEMA_REGISTRY_AUTH_BEARER_KEY = "schema.registry.bearer";

	public static final String SCHEMA_REGISTRY_AUTH_FLAG = "schema.registry.auth.enabled";

	public static final String ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG = "auto.register.schemas";

	public static final String ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG_DEFAULT = "false";
}
