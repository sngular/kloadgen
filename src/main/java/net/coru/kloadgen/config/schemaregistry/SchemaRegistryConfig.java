package net.coru.kloadgen.config.schemaregistry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = SchemaRegistryConfig.SchemaRegistryConfigBuilder.class)
public class SchemaRegistryConfig {

	String schemaRegistryUrl;

	String username;

	String password;

	@JsonPOJOBuilder(withPrefix = "")
	public static final class SchemaRegistryConfigBuilder {
	}

}
