/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.model.json;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
public class NumberField extends Field {

  String defaultValue;

  Number minimum;

  Number maximum;

  Number exclusiveMinimum;

  Number exclusiveMaximum;

  Number multipleOf;

  @Builder(toBuilder = true)
  public NumberField(
      final String name, final String defaultValue, final Number minimum, final Number maximum, final Number exclusiveMinimum,
      final Number exclusiveMaximum, final Number multipleOf) {
    super(name, "number");
    this.defaultValue = defaultValue;
    this.maximum = maximum;
    this.minimum = minimum;
    this.exclusiveMinimum = exclusiveMinimum;
    this.exclusiveMaximum = exclusiveMaximum;
    this.multipleOf = multipleOf;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NumberField)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final NumberField that = (NumberField) o;
    return Objects.equals(defaultValue, that.defaultValue)
           && compare(minimum, that.minimum)
           && compare(maximum, that.maximum)
           && compare(exclusiveMinimum, that.exclusiveMinimum)
           && compare(exclusiveMaximum, that.exclusiveMaximum)
           && compare(multipleOf, that.multipleOf)
           && Objects.equals(super.getName(), ((NumberField) o).getName())
           && Objects.equals(super.getType(), ((NumberField) o).getType());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(super.hashCode(), defaultValue, minimum, maximum, exclusiveMinimum, exclusiveMaximum, multipleOf);
  }

  @Override
  public final Field cloneField(final String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }

  @Override
  public final List<Field> getProperties() {
    return Collections.singletonList(this);
  }

  public final boolean compare(final Number number1, final Number number2) {
    boolean equals = false;
    if (Objects.equals(number1, number2)) {
      equals = true;
    } else if (Objects.nonNull(number1)) {
      equals = number1.toString().equalsIgnoreCase(number2.toString());
    }
    return equals;
  }
}

