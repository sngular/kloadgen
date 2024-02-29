/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HeaderMappingTest {

  private static HeaderMapping headerMapping;

  @BeforeAll
  static void setUpTest() {
    headerMapping = HeaderMapping
                        .builder()
                        .headerName("value")
                        .headerValue("String")
                        .build();
  }

  @Test
  void getHeaderName() {
    Assertions.assertThat(headerMapping).hasFieldOrPropertyWithValue("headerName", "value");
  }

  @Test
  void getHeaderValue() {
    Assertions.assertThat(headerMapping).hasFieldOrPropertyWithValue("headerValue", "String");
  }
}


