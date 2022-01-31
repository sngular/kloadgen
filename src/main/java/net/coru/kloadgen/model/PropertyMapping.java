/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.model;

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

  public PropertyMapping(String propertyName, String propertyValue) {
    this.setPropertyName(propertyName);
    this.setPropertyValue(propertyValue);
    init();
  }

  public String getPropertyName() {
    return getPropertyAsString(PROPERTY_NAME);
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
    setProperty(PROPERTY_NAME, propertyName);
  }

  public String getPropertyValue() {
    return getPropertyAsString(PROPERTY_VALUE);
  }

  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
    setProperty(PROPERTY_VALUE, propertyValue);
  }

  public void init() {
    this.setName("Config properties");
  }
}
