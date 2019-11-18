package net.coru.kloadgen.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.coru.kloadgen.config.plaintext.PlainTextConfigElementBeanInfo;
import net.coru.kloadgen.config.serialized.SerializedConfigElementBeanInfo;
import net.coru.kloadgen.input.serialized.ClassPropertyEditor;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.config.plaintext.PlainTextConfigElement;
import net.coru.kloadgen.config.serialized.SerializedConfigElement;
import net.coru.kloadgen.util.PropsKeys;
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
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import java.beans.PropertyDescriptor;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PepperBoxConfigElementTest {

    @BeforeEach
    public static void setUp(){
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
    public void plainTextConfigTest(){

        PlainTextConfigElement plainTextConfigElement = new PlainTextConfigElement();
        plainTextConfigElement.setJsonSchema(TestInputUtils.testSchema);
        plainTextConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
        plainTextConfigElement.iterationStart(null);
        Object object = JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
        DocumentContext doc = JsonPath.parse(object.toString());
        com.revinate.assertj.json.JsonPathAssert.assertThat(doc).jsonPathAsInteger("$.messageId").isGreaterThan(0);
    }

    @Test
    public void plainTextExceptionTest(){

        assertThrows( ClassFormatError.class, () -> {
            PlainTextConfigElement plainTextConfigElement = new PlainTextConfigElement();
            plainTextConfigElement.setJsonSchema(TestInputUtils.defectSchema);
            plainTextConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
            JMeterContextService.getContext().getVariables().remove(PropsKeys.MSG_PLACEHOLDER);
            plainTextConfigElement.iterationStart(null);
            Object object = JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
        }, "Failed to run config element");
    }

    @Test
    public void serializedConfigTest(){

        List<FieldValueMapping> fieldValueMappings = TestInputUtils.getValueExpressionMappings();
        SerializedConfigElement serializedConfigElement = new SerializedConfigElement();
        serializedConfigElement.setClassName("com.gslab.pepper.test.Message");
        serializedConfigElement.setObjProperties(fieldValueMappings);
        serializedConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
        JMeterContextService.getContext().getVariables().remove(PropsKeys.MSG_PLACEHOLDER);
        serializedConfigElement.iterationStart(null);
        Message message = (Message)JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
        assertThat( message.getMessageBody()).as("Failed to run config element").isEqualTo("Test Message");

    }

    public void serializedConfigErrorTest(){

        List<FieldValueMapping> fieldValueMappings = TestInputUtils.getWrongValueExpressionMappings();
        SerializedConfigElement serializedConfigElement = new SerializedConfigElement();
        serializedConfigElement.setClassName("com.gslab.pepper.test.Message");
        serializedConfigElement.setObjProperties(fieldValueMappings);
        serializedConfigElement.setPlaceHolder(PropsKeys.MSG_PLACEHOLDER);
        JMeterContextService.getContext().getVariables().remove(PropsKeys.MSG_PLACEHOLDER);
        serializedConfigElement.iterationStart(null);
        Message message = (Message)JMeterContextService.getContext().getVariables().getObject(PropsKeys.MSG_PLACEHOLDER);
        assertThat(message).as("Failed to run config element").isNotNull();

    }

    @Test
    public void validateSchemaProcessor(){

        try {
            SchemaProcessor schemaProcessor = new SchemaProcessor();
            assertThat(schemaProcessor.getPlainTextMessageIterator(TestInputUtils.testSchema)).as("Failed to generate Iterator from input schema").isInstanceOf(Iterator.class);
        } catch (Exception e) {
            fail("Failed to generate Iterator from input schema : " + e.getMessage());
        }
    }


    @Test
    public void validateClassPropertyEditor() {
        assertThrows(Exception.class, () -> {
            ResourceBundle.getBundle(PlainTextConfigElement.class.getName());
            PlainTextConfigElementBeanInfo pbeanInfo = new PlainTextConfigElementBeanInfo();
            assertThat(pbeanInfo.getPropertyDescriptors()).as("Failed to validate serialized property descriptors").hasSize(3);

            ResourceBundle.getBundle(SerializedConfigElement.class.getName());
            SerializedConfigElementBeanInfo sbeanInfo = new SerializedConfigElementBeanInfo();
            assertThat(sbeanInfo.getPropertyDescriptors()).as("Failed to validate serialized property descriptors").hasSize(3);

            PropertyDescriptor propertyDescriptor = sbeanInfo.getPropertyDescriptors()[1];
            ClassPropertyEditor classPropertyEditor = new ClassPropertyEditor(propertyDescriptor);
            classPropertyEditor.setValue("com.gslab.pepper.test.Message");
            classPropertyEditor.actionPerformed(null);
            assertThat(classPropertyEditor.getValue()).as("Failed to validate serialized property descriptors").isEqualTo("com.gslab.pepper.test.Message");

        });
    }
}
