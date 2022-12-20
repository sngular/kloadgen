/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.model.json;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ArrayField extends Field {

  List<Field> values;

  int minItems;

  boolean uniqueItems;

  boolean isFieldRequired;

  @Builder(toBuilder = true)
  public ArrayField(final String name, final List<Field> values, final int minItems, final boolean uniqueItems, final boolean isFieldRequired) {
    super(name, "array");
    this.values = values;
    this.minItems = minItems;
    this.uniqueItems = uniqueItems;
    this.isFieldRequired = isFieldRequired;
  }

  @Override
  public final Field cloneField(final String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }

  @Override
  public final List<Field> getProperties() {
    return values;
  }

  public static class ArrayFieldBuilder {

    private final List<Field> values = new ArrayList<>();

    public final ArrayFieldBuilder values(final List<Field> values) {
      this.values.addAll(values);
      return this;
    }

    public final ArrayFieldBuilder value(final Field value) {
      this.values.add(value);
      return this;
    }
  }
}
