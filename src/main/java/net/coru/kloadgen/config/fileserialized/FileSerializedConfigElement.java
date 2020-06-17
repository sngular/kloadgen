package net.coru.kloadgen.config.fileserialized;

import static net.coru.kloadgen.util.ProducerKeysHelper.SAMPLE_ENTITY;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.loadgen.impl.AvroLoadGenerator;
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

  private BaseLoadGenerator generator;

  private String avroSchema;

  @Override
  public void iterationStart(LoopIterationEvent loopIterationEvent) {

    try {
      if (generator ==  null) {
        generator = new AvroLoadGenerator(avroSubject, schemaProperties);
      }

      JMeterVariables variables = JMeterContextService.getContext().getVariables();
      variables.putObject(SAMPLE_ENTITY, generator.nextMessage());
    } catch (Exception e) {
      log.error("Failed to create AvroLoadGenerator instance", e);
    }
  }

}
