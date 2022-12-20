/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.model.json;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IntegerField extends Field {

  String defaultValue;

  int minimum;

  int maximum;

  @Builder(toBuilder = true)
  public IntegerField(final String name, final String defaultValue, final int minimum, final int maximum) {
    super(name, "number");
    this.defaultValue = defaultValue;
    this.maximum = maximum;
    this.minimum = minimum;
  }

  @Override
  public Field cloneField(final String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }

  @Override
  public List<Field> getProperties() {
    return Collections.singletonList(this);
  }
}
