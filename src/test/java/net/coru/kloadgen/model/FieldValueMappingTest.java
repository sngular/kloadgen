/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FieldValueMappingTest {

  private static FieldValueMapping fieldValueMapping;

  @BeforeAll
  static void setUpTest() {
    fieldValueMapping = FieldValueMapping
        .builder()
        .fieldName("value")
        .fieldType("String")
        .valueLength(30)
        .fieldValueList(
            "{\"client_code\":\"ABC\",\"market_codes\":[\"popfa\",\"popfa\"],\"permissions\":[{\"app_code\":\"TEAA\",\"resource_codes\":[{\"code\":\"\",\"action\":\"\"}]}]}," +
            "{\"client_code\":\"ABC\",\"market_codes\":[\"popfa\",\"popfa\"],\"permissions\":[{\"app_code\":\"TEAA\",\"resource_codes\":[{\"code\":\"\",\"action\":\"\"}]}]}")
        .constraint(ConstraintTypeEnum.MAXIMUM_VALUE, "5")
        .build();
  }

  @Test
  void getFieldName() {
    assertThat(fieldValueMapping).hasFieldOrPropertyWithValue("fieldName", "value");
  }

  @Test
  void getValueLength() {
    assertThat(fieldValueMapping).hasFieldOrPropertyWithValue("valueLength", 30);
  }

  @Test
  void getFieldType() {
    assertThat(fieldValueMapping).hasFieldOrPropertyWithValue("fieldType", "String");
  }

  @Test
  void getFieldValuesList() {
    assertThat(fieldValueMapping.getFieldValuesList())
        .hasSize(2)
        .containsExactlyElementsOf(List.of(
            "{\"client_code\":\"ABC\",\"market_codes\":[\"popfa\",\"popfa\"],\"permissions\":[{\"app_code\":\"TEAA\",\"resource_codes\":[{\"code\":\"\",\"action\":\"\"}]}]}",
            "{\"client_code\":\"ABC\",\"market_codes\":[\"popfa\",\"popfa\"],\"permissions\":[{\"app_code\":\"TEAA\",\"resource_codes\":[{\"code\":\"\",\"action\":\"\"}]}]}"));
  }

  @Test
  void getFieldValuesListSingleJson() {
    fieldValueMapping.setFieldValuesList(
        "{\"client_code\":\"ABC\",\"market_codes\":[\"popfa\",\"popfa\"],\"permissions\":[{\"app_code\":\"TEAA\",\"resource_codes\":[{\"code\":\"jj\",\"action\":\"kk\"}]}]}");
    assertThat(fieldValueMapping.getFieldValuesList())
        .hasSize(1)
        .containsExactlyElementsOf(List.of(
            "{\"client_code\":\"ABC\",\"market_codes\":[\"popfa\",\"popfa\"],\"permissions\":[{\"app_code\":\"TEAA\",\"resource_codes\":[{\"code\":\"jj\",\"action\":\"kk\"}]}]}"));
  }
}