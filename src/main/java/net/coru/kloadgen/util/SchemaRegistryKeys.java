package net.coru.kloadgen.util;

public class SchemaRegistryKeys {

	public static final String SCHEMA_REGISTRY_USERNAME_KEY = "schema.registry.username";
	public static final String SCHEMA_REGISTRY_USERNAME_DEFAULT = "<username>";

	public static final String SCHEMA_REGISTRY_PASSWORD_KEY = "schema.registry.password";
	public static final String SCHEMA_REGISTRY_PASSWORD_DEFAULT = "<password>";

	public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
	public static final String SCHEMA_REGISTRY_DEFAULT = "http://localhost:8081";

	public static final String SCHEMA_REGISTRY_CONFIG_DEFAULT = "{\"schemaRegistryUrl\":\"" + SCHEMA_REGISTRY_DEFAULT + "\"," +
				"\"username\":\"" + SCHEMA_REGISTRY_USERNAME_DEFAULT + "\","+
					"\"password\":\"" + SCHEMA_REGISTRY_PASSWORD_DEFAULT + "\"}";
}
