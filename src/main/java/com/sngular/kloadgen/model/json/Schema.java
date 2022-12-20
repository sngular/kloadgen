/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.model.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class Schema {

  String id;

  String name;

  String type;

  List<String> requiredFields;

  List<Field> properties;

  List<Field> definitions;

  public static class SchemaBuilder {

    List<Field> properties = new ArrayList<>();

    List<Field> definitions = new ArrayList<>();

    List<String> requiredFields = new ArrayList<>();

    public final SchemaBuilder property(final Field field) {
      properties.add(field);
      return this;
    }

    public final SchemaBuilder properties(final List<Field> fieldList) {
      properties.addAll(fieldList);
      return this;
    }

    public final SchemaBuilder description(final Field field) {
      definitions.add(field);
      return this;
    }

    public final SchemaBuilder descriptions(final List<Field> fieldList) {
      definitions.addAll(fieldList);
      return this;
    }

    public final SchemaBuilder descriptions(final Collection<Field> fieldList) {
      definitions.addAll(fieldList);
      return this;
    }

    public final SchemaBuilder requiredField(final String field) {
      requiredFields.add(field);
      return this;
    }

    public final SchemaBuilder requiredFields(final List<String> fields) {
      requiredFields.addAll(fields);
      return this;
    }

    public final SchemaBuilder requiredFields(final String[] fields) {
      requiredFields.addAll(Arrays.asList(fields));
      return this;
    }
  }

}
