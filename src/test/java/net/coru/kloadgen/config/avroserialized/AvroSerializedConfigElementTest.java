package net.coru.kloadgen.config.avroserialized;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import java.util.EnumSet;
import java.util.Set;
import net.coru.kloadgen.test.TestInputUtils;
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
    Configuration.setDefaults(new Configuration.Defaults() {
      final JsonProvider jsonProvider = new JacksonJsonProvider();

      final MappingProvider mappingProvider = new JacksonMappingProvider();

      @Override
      public JsonProvider jsonProvider() {
        return jsonProvider;
      }

      @Override
      public MappingProvider mappingProvider() {
        return mappingProvider;
      }

      @Override
      public Set<Option> options() {
        return EnumSet.noneOf(Option.class);
      }
    });
  }
  @Test
  public void avroSerializedConfigTest() {

    AvroSerializedConfigElement plainTextConfigElement = new AvroSerializedConfigElement();
    plainTextConfigElement.setAvroSubject("AvroSubject");
    plainTextConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
    plainTextConfigElement.iterationStart(null);
    Object object = JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
    DocumentContext doc = JsonPath.parse(object.toString());
    com.revinate.assertj.json.JsonPathAssert.assertThat(doc).jsonPathAsInteger("$.messageId").isGreaterThan(0);
  }

  @Test
  void iterationStart() {
  }
}