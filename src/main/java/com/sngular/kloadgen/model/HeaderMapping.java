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
public class HeaderMapping extends AbstractTestElement {

  public static final String HEADER_NAME = "headerName";

  public static final String HEADER_VALUE = "headerValue";

  private String headerName;

  private String headerValue;

  public HeaderMapping(final String headerName, final String headerValue) {
    this.setHeaderName(headerName);
    this.setHeaderValue(headerValue);
    init();
  }

  public final void init() {
    this.setName("Header Field");
  }

  public final String getHeaderName() {
    return getPropertyAsString(HEADER_NAME);
  }

  public final void setHeaderName(final String headerName) {
    this.headerName = headerName;
    setProperty(HEADER_NAME, headerName);
  }

  public final String getHeaderValue() {
    return getPropertyAsString(HEADER_VALUE);
  }

  public final void setHeaderValue(final String headerValue) {
    this.headerValue = headerValue;
    setProperty(HEADER_VALUE, headerValue);
  }
}
