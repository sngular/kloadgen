package net.coru.kloadgen.input.avro;

import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_PASSWORD_KEY;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL;
import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.SCHEMA_REGISTRY_USERNAME_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.github.tomakehurst.wiremock.WireMockServer;
import net.coru.kloadgen.config.avroserialized.AvroSerializedConfigElement;
import net.coru.kloadgen.model.FieldValueMapping;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

@ExtendWith({
    WiremockResolver.class,
    WiremockUriResolver.class
})
class AvroSubjectPropertyEditorTest {

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
  public void iterationStart(@Wiremock WireMockServer server) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    AvroSerializedConfigElement avroSerializedConfigElement = new AvroSerializedConfigElement("AvroSubject", Collections.emptyList(), null);
    JMeterVariables variables = JMeterContextService.getContext().getVariables();
    avroSerializedConfigElement.iterationStart(null);
    assertThat(variables).isNotNull();
  }

  @Test
  public void mergeValueTest_schemaRegistryEmpty_returnEmpty() {

    FieldValueMapping fieldValue = new FieldValueMapping("field1", "string");

    List<FieldValueMapping> atributeListTable = new ArrayList<FieldValueMapping>();
    atributeListTable.add(fieldValue);

    List<FieldValueMapping> attributeList = new ArrayList<FieldValueMapping>();

    AvroSubjectPropertyEditor avroSubjectPropertyEditor = new AvroSubjectPropertyEditor();
    List<FieldValueMapping> result = avroSubjectPropertyEditor.mergeValue(atributeListTable, attributeList);

    assertNotNull(result);
    assertTrue(result.isEmpty());

  }

  @Test
  public void mergeValueTest_schemaRegistryWithDistinctName_returnResultOneItem() {

    FieldValueMapping fieldValue = new FieldValueMapping("field1", "string");

    List<FieldValueMapping> atributeListTable = new ArrayList<FieldValueMapping>();
    atributeListTable.add(fieldValue);

    FieldValueMapping fieldValueSchema = new FieldValueMapping("fieldSchema1", "string");
    List<FieldValueMapping> attributeList = new ArrayList<FieldValueMapping>();
    attributeList.add(fieldValueSchema);

    AvroSubjectPropertyEditor avroSubjectPropertyEditor = new AvroSubjectPropertyEditor();
    List<FieldValueMapping> result = avroSubjectPropertyEditor.mergeValue(atributeListTable, attributeList);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(result.get(0).getFieldName(), fieldValueSchema.getFieldName());

  }

  @Test
  public void mergeValueTest_schemaRegistryWithDistinctType_returnResultOneItem() {

    FieldValueMapping fieldValue = new FieldValueMapping("field1", "string");

    List<FieldValueMapping> atributeListTable = new ArrayList<FieldValueMapping>();
    atributeListTable.add(fieldValue);

    FieldValueMapping fieldValueSchema = new FieldValueMapping("field1", "int");
    List<FieldValueMapping> attributeList = new ArrayList<FieldValueMapping>();
    attributeList.add(fieldValueSchema);

    AvroSubjectPropertyEditor avroSubjectPropertyEditor = new AvroSubjectPropertyEditor();
    List<FieldValueMapping> result = avroSubjectPropertyEditor.mergeValue(atributeListTable, attributeList);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(result.get(0).getFieldName(), fieldValueSchema.getFieldName());
    assertEquals(result.get(0).getFieldType(), fieldValueSchema.getFieldType());

  }

  @Test
  public void mergeValueTest_schemaRegistryWithSameValue_returnResultOneItem() {

    FieldValueMapping fieldValue = new FieldValueMapping("field1", "string");

    List<FieldValueMapping> atributeListTable = new ArrayList<FieldValueMapping>();
    atributeListTable.add(fieldValue);

    FieldValueMapping fieldValueSchema = new FieldValueMapping("field1", "string");
    List<FieldValueMapping> attributeList = new ArrayList<FieldValueMapping>();
    attributeList.add(fieldValueSchema);

    AvroSubjectPropertyEditor avroSubjectPropertyEditor = new AvroSubjectPropertyEditor();
    List<FieldValueMapping> result = avroSubjectPropertyEditor.mergeValue(atributeListTable, attributeList);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(result.get(0).getFieldName(), fieldValueSchema.getFieldName());
    assertEquals(result.get(0).getFieldType(), fieldValueSchema.getFieldType());
    assertEquals(result.get(0).getFieldName(), fieldValue.getFieldName());
    assertEquals(result.get(0).getFieldType(), fieldValue.getFieldType());

  }

  @Test
  public void mergeValueTest_schemaRegistryWithTwoValues_returnResultOneItem() {

    FieldValueMapping fieldValue = new FieldValueMapping("field1", "string", 0, "[\"value1\"]");

    List<FieldValueMapping> atributeListTable = new ArrayList<FieldValueMapping>();
    atributeListTable.add(fieldValue);

    FieldValueMapping fieldValueSchema = new FieldValueMapping("field1", "string");
    FieldValueMapping fieldValueSchema2 = new FieldValueMapping("field2", "string");
    List<FieldValueMapping> attributeList = new ArrayList<FieldValueMapping>();
    attributeList.add(fieldValueSchema);
    attributeList.add(fieldValueSchema2);

    AvroSubjectPropertyEditor avroSubjectPropertyEditor = new AvroSubjectPropertyEditor();
    List<FieldValueMapping> result = avroSubjectPropertyEditor.mergeValue(atributeListTable, attributeList);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertEquals(result.get(0).getFieldName(), fieldValueSchema.getFieldName());
    assertEquals(result.get(0).getFieldType(), fieldValueSchema.getFieldType());
    assertEquals(result.get(0).getFieldName(), fieldValue.getFieldName());
    assertEquals(result.get(0).getFieldType(), fieldValue.getFieldType());
    assertEquals(result.get(0).getFieldValuesList(), fieldValue.getFieldValuesList());
    assertEquals(result.get(1).getFieldName(), fieldValueSchema2.getFieldName());
    assertEquals(result.get(1).getFieldType(), fieldValueSchema2.getFieldType());

  }

}