package net.coru.kloadgen.config.fileserialized;

import static net.coru.kloadgen.util.PropsKeysHelper.AVRO_SCHEMA;
import static net.coru.kloadgen.util.PropsKeysHelper.SCHEMA_PROPERTIES;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.model.FieldValueMapping;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class FileSerializedConfigElement  extends ConfigTestElement implements TestBean, LoopIterationListener {

  private String avroSubject;

  private List<FieldValueMapping> schemaProperties;

  private String avroSchema;

  @Override
  public void iterationStart(LoopIterationEvent loopIterationEvent) {

    JMeterVariables variables = JMeterContextService.getContext().getVariables();
    variables.putObject(AVRO_SCHEMA, avroSchema);
    variables.putObject(SCHEMA_PROPERTIES, schemaProperties);
  }

}
