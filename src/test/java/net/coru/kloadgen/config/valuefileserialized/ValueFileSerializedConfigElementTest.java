package net.coru.kloadgen.config.valuefileserialized;

import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SCHEMA_PROPERTIES;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SUBJECT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import java.io.File;
import java.util.Collections;
import java.util.Locale;
import net.coru.kloadgen.serializer.AvroSerializer;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValueFileSerializedConfigElementTest {

  @BeforeEach
  public void setUp() {
    File file = new File("src/test/resources");
    String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  public void iterationStart() {

    String definitionSchema = "{\"type\": \"record\", \"name\": \"value\", \"namespace\": \"my.topic\", \"fields\": [  {   \"name\": \"myValue\",   \"type\": \"long\"  } ], \"connect.name\": \"my.topic.value\"}";

    ValueFileSerializedConfigElement
        valueFileSerializedConfigElement =
        new ValueFileSerializedConfigElement("avroSubject", Collections.emptyList(), definitionSchema, "AVRO",
           AvroSerializer.class.getSimpleName(), TopicNameStrategy.class.getSimpleName());
    valueFileSerializedConfigElement.iterationStart(null);
    assertThat(JMeterContextService.getContext().getVariables().getObject(VALUE_SUBJECT_NAME)).isNotNull();
    assertThat(JMeterContextService.getContext().getVariables().getObject(VALUE_SCHEMA_PROPERTIES)).isNotNull();

  }

}