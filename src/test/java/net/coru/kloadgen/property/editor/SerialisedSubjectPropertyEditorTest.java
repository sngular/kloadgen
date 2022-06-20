/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.property.editor;

import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SCHEMA_PROPERTIES;
import static net.coru.kloadgen.util.PropsKeysHelper.VALUE_SUBJECT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import net.coru.kloadgen.config.valueserialized.ValueSerializedConfigElement;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.AvroSerializer;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SerialisedSubjectPropertyEditorTest {

  private static Stream<Arguments> parametersForMergeValue() {
    return Stream.of(Arguments.of(new ArrayList<FieldValueMapping>(), new ArrayList<FieldValueMapping>(), new ArrayList<FieldValueMapping>()),
                     Arguments.of(new ArrayList<>(Collections.singletonList(FieldValueMapping.builder().fieldName("fieldName").fieldType("fieldType").required(true)
                                                                                             .isAncestorRequired(true).build())),
                                  new ArrayList<FieldValueMapping>(),
                                  new ArrayList<FieldValueMapping>()),
                     Arguments.of(new ArrayList<>(Collections.singletonList(FieldValueMapping.builder().fieldName("fieldName").fieldType("fieldType").required(true)
                                                                                             .isAncestorRequired(true).build())),
                                  Collections.singletonList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").required(true)
                                                                             .isAncestorRequired(true).build()),
                                  Collections.singletonList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").required(true)
                                                                             .isAncestorRequired(true).build())),
                     Arguments.of(new ArrayList<>(Collections.singletonList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("int").required(true)
                                                                                             .isAncestorRequired(true).build())),
                                  Collections.singletonList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").required(true)
                                                                             .isAncestorRequired(true).build()),
                                  Collections.singletonList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").required(true)
                                                                             .isAncestorRequired(true).build())),
                     Arguments.of(new ArrayList<>(Collections.singletonList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").required(true)
                                                                                             .isAncestorRequired(true).build())),
                                  Collections.singletonList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").required(true)
                                                                             .isAncestorRequired(true).build()),
                                  Collections.singletonList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").required(true)
                                                                             .isAncestorRequired(true).build())),
                     Arguments.of(new ArrayList<>(
                                      Collections.singletonList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").fieldValueList("[\"value1\"]")
                                                                                 .required(true).isAncestorRequired(true).build())),
                                  Arrays.asList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").required(true).isAncestorRequired(true).build(),
                                                FieldValueMapping.builder().fieldName("field2").fieldType("string").required(true).isAncestorRequired(true).build()),
                                  Arrays.asList(FieldValueMapping.builder().fieldName("fieldSchema1").fieldType("string").fieldValueList("[\"value1\"]").required(true)
                                                                 .isAncestorRequired(true).build(),
                                                FieldValueMapping.builder().fieldName("field2").fieldType("string").required(true).isAncestorRequired(true).build())));
  }

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
  @DisplayName("Should Serialised Subject Property")
  void iterationStart() {

    ValueSerializedConfigElement
        valueSerializedConfigElement = new ValueSerializedConfigElement("avroSubject", Collections.emptyList(), "AVRO",
                                                                        AvroSerializer.class.getSimpleName(), TopicNameStrategy.class.getSimpleName());
    JMeterVariables variables = JMeterContextService.getContext().getVariables();
    valueSerializedConfigElement.iterationStart(null);

    assertThat(variables).isNotNull();
    assertThat(variables.getObject(VALUE_SUBJECT_NAME)).isNotNull();
    assertThat(variables.getObject(VALUE_SCHEMA_PROPERTIES)).isNotNull();

  }

  @ParameterizedTest
  @MethodSource("parametersForMergeValue")
  @DisplayName("Should Merge Schema Properties Property")
  void mergeValueTest(List<FieldValueMapping> attributeListTable, List<FieldValueMapping> attributeList, List<FieldValueMapping> expected) {

    final List<FieldValueMapping> result = new SerialisedSubjectPropertyEditor().mergeValue(attributeListTable, attributeList);

    assertThat(result).isEqualTo(expected);

  }
}