package net.coru.kloadgen.config.schemaregistry;

import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG;
import static io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig.USER_INFO_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.FLAG_YES;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_BASIC_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_BEARER_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_BEARER_TYPE;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_FLAG;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_AUTH_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_URL;
import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_USERNAME_KEY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.PropertyMapping;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class SchemaRegistryConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String schemaRegistryUrl;

  private List<PropertyMapping> schemaRegistryProperties;

  public void setSchemaRegistryProperties(List<PropertyMapping> schemaRegistryProperties) {
    this.schemaRegistryProperties = schemaRegistryProperties;
    serializeProperties(schemaRegistryProperties);
  }

  @Override
  public void iterationStart(LoopIterationEvent iterEvent) {
    serializeProperties(schemaRegistryProperties);
  }

  private void serializeProperties(List<PropertyMapping> schemaRegistryProperties) {
    Properties contextProperties = getThreadContext().getProperties();

    Map<String, String> schemaProperties = fromListToPropertiesMap(schemaRegistryProperties);

    contextProperties.setProperty(SCHEMA_REGISTRY_URL, schemaRegistryUrl);
    if (FLAG_YES.equalsIgnoreCase(schemaProperties.get(SCHEMA_REGISTRY_AUTH_FLAG))) {
      contextProperties.setProperty(SCHEMA_REGISTRY_AUTH_FLAG, FLAG_YES);
      if (SCHEMA_REGISTRY_AUTH_BASIC_TYPE.equalsIgnoreCase(schemaProperties.get(SCHEMA_REGISTRY_AUTH_KEY))) {
        contextProperties.setProperty(SCHEMA_REGISTRY_AUTH_KEY, SCHEMA_REGISTRY_AUTH_BASIC_TYPE);
        contextProperties.setProperty(BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
        contextProperties.setProperty(USER_INFO_CONFIG,
            schemaProperties.get(SCHEMA_REGISTRY_USERNAME_KEY) + ":" + schemaProperties.get(SCHEMA_REGISTRY_PASSWORD_KEY));
      } else if (SCHEMA_REGISTRY_AUTH_BEARER_TYPE.equalsIgnoreCase(schemaProperties.get(SCHEMA_REGISTRY_AUTH_KEY))) {
        contextProperties.setProperty(SCHEMA_REGISTRY_AUTH_KEY, SCHEMA_REGISTRY_AUTH_BEARER_TYPE);
        contextProperties.setProperty(BEARER_AUTH_CREDENTIALS_SOURCE, "STATIC_TOKEN");
        contextProperties.setProperty(BEARER_AUTH_TOKEN_CONFIG, schemaProperties.get(SCHEMA_REGISTRY_AUTH_BEARER_KEY));
      }
    }
  }

  private Map<String, String> fromListToPropertiesMap(List<PropertyMapping> schemaProperties) {
    Map<String, String> propertiesMap = new HashMap<>();
    for (PropertyMapping property : schemaProperties) {
      propertiesMap.put(property.getPropertyName(), property.getPropertyValue());
    }
    return propertiesMap;
  }
}
