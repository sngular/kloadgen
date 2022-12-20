/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.property.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.sngular.kloadgen.config.valueserialized.ValueSerializedConfigElement;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.serializer.AvroSerializer;
import com.sngular.kloadgen.util.PropsKeysHelper;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.assertj.core.api.Assertions;
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
    final File file = new File("src/test/resources");
    final String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    final JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  @DisplayName("Should Serialised Subject Property")
  void iterationStart() {

    final var valueSerializedConfigElement = new ValueSerializedConfigElement("avroSubject", Collections.emptyList(), "AVRO",
                                                                        AvroSerializer.class.getSimpleName(), TopicNameStrategy.class.getSimpleName());
    final var variables = JMeterContextService.getContext().getVariables();
    valueSerializedConfigElement.iterationStart(null);

    Assertions.assertThat(variables).isNotNull();
    Assertions.assertThat(variables.getObject(PropsKeysHelper.VALUE_SUBJECT_NAME)).isNotNull();
    Assertions.assertThat(variables.getObject(PropsKeysHelper.VALUE_SCHEMA_PROPERTIES)).isNotNull();

  }

  @ParameterizedTest
  @MethodSource("parametersForMergeValue")
  @DisplayName("Should Merge Schema Properties Property")
  void mergeValueTest(final List<FieldValueMapping> attributeListTable, final List<FieldValueMapping> attributeList, final List<FieldValueMapping> expected) {

    final List<FieldValueMapping> result = new SerialisedSubjectPropertyEditor().mergeValue(attributeListTable, attributeList);

    Assertions.assertThat(result).isEqualTo(expected);

  }
}