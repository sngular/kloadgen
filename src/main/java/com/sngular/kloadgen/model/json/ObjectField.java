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
public class ObjectField extends Field {

  List<Field> properties;

  List<String> required;

  boolean isFieldRequired;

  @Builder(toBuilder = true)
  public ObjectField(final String name, final List<Field> properties, final List<String> required, final boolean isFieldRequired) {
    super(name, "object");
    this.properties = properties;
    this.required = required;
    this.isFieldRequired = isFieldRequired;
  }

  @Override
  public final List<Field> getProperties() {
    return properties;
  }

  @Override
  public final Field cloneField(final String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }

  public static class ObjectFieldBuilder {

    private final List<Field> properties = new ArrayList<>();

    private List<String> required = new ArrayList<>();

    public final ObjectFieldBuilder properties(final List<Field> fieldList) {
      properties.addAll(fieldList);
      return this;
    }

    public final ObjectFieldBuilder property(final Field field) {
      properties.add(field);
      return this;
    }

    public final ObjectFieldBuilder required(final String requiredField) {
      required.add(requiredField);
      return this;
    }

    public final ObjectFieldBuilder required(final List<String> requiredFields) {
      required = requiredFields;
      return this;
    }
  }

}
