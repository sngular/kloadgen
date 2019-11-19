package net.coru.kloadgen.config.avroserialized;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.coru.kloadgen.util.PropsKeys;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvroSerializedConfigElementTest {


  @BeforeEach
  public static void setUp() {
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
  }
  @Test
  public void avroSerializedConfigTest() {

    AvroSerializedConfigElement avroSerializedConfigElement = new AvroSerializedConfigElement();
    avroSerializedConfigElement.setAvroSubject("AvroSubject");
    avroSerializedConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
    avroSerializedConfigElement.iterationStart(null);
    Object object = JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
    DocumentContext doc = JsonPath.parse(object.toString());
    com.revinate.assertj.json.JsonPathAssert.assertThat(doc).jsonPathAsInteger("$.messageId").isGreaterThan(0);
  }

  @Test
  void iterationStart() {
  }
}