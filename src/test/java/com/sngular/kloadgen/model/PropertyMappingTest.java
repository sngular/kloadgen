package com.sngular.kloadgen.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PropertyMappingTest {
  private static PropertyMapping propertyMapping;

  @BeforeAll
  static void setUpTest() {
    propertyMapping = PropertyMapping
                        .builder()
                        .propertyName("value")
                        .propertyValue("String")
                        .build();
  }

  @Test
  void getPropertyName() {
    Assertions.assertThat(propertyMapping).hasFieldOrPropertyWithValue("propertyName", "value");
  }

  @Test
  void getPropertyValue() {
    Assertions.assertThat(propertyMapping).hasFieldOrPropertyWithValue("propertyValue", "String");
  }
}
