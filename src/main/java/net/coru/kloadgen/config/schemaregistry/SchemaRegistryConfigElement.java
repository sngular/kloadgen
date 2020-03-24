package net.coru.kloadgen.config.schemaregistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContext;

import java.util.Map;

import static net.coru.kloadgen.util.SchemaRegistryKeys.*;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class SchemaRegistryConfigElement extends ConfigTestElement implements TestBean, LoopIterationListener {

  ObjectMapper mapper = new ObjectMapper();

  private SchemaRegistryConfig schemaRegistryConfig;

  @Override
  public void iterationStart(LoopIterationEvent iterEvent) {

    JMeterContext context = getThreadContext();

    Map<String, Object> threadVars = context.getSamplerContext();

    threadVars.put(SCHEMA_REGISTRY_URL, schemaRegistryConfig.getSchemaRegistryUrl());
    threadVars.put(SCHEMA_REGISTRY_USERNAME_KEY, schemaRegistryConfig.getUsername());
    threadVars.put(SCHEMA_REGISTRY_PASSWORD_KEY, schemaRegistryConfig.getPassword());

  }

  @SneakyThrows
  @Override
  public String getPropertyAsString(String key) {
    String schemaRegistryConfigAsString;
    if ("schemaRegistryConfig".equalsIgnoreCase(key)) {
      schemaRegistryConfigAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaRegistryConfig);
    } else {
      schemaRegistryConfigAsString = super.getPropertyAsString(key);
    }
    return schemaRegistryConfigAsString;
  }

  @SneakyThrows
  @Override
  public void setProperty(String name, String value) {
    if ("schemaRegistryConfig".equalsIgnoreCase(name)) {
      schemaRegistryConfig = mapper.readerFor(SchemaRegistryConfig.class).readValue(value);
    } else {
      super.setProperty(name, value);
    }
  }
}
