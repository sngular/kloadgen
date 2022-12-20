/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.jmeter.testelement.AbstractTestElement;

@Builder
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PropertyMapping extends AbstractTestElement {

  public static final String PROPERTY_NAME = "propertyName";

  public static final String PROPERTY_VALUE = "propertyValue";

  private String propertyName;

  private String propertyValue;

  public PropertyMapping(final String propertyName, final String propertyValue) {
    this.setPropertyName(propertyName);
    this.setPropertyValue(propertyValue);
    init();
  }

  public final void init() {
    this.setName("Config properties");
  }

  public final String getPropertyName() {
    return getPropertyAsString(PROPERTY_NAME);
  }

  public final void setPropertyName(final String propertyName) {
    this.propertyName = propertyName;
    setProperty(PROPERTY_NAME, propertyName);
  }

  public final String getPropertyValue() {
    return getPropertyAsString(PROPERTY_VALUE);
  }

  public final void setPropertyValue(final String propertyValue) {
    this.propertyValue = propertyValue;
    setProperty(PROPERTY_VALUE, propertyValue);
  }
}
