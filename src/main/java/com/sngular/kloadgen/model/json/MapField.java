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
public class MapField extends Field {

  Field mapType;

  boolean isFieldRequired;

  @Builder(toBuilder = true)
  public MapField(final String name, final Field mapType, final boolean isFieldRequired) {
    super(name, "map");
    this.mapType = mapType;
    this.isFieldRequired = isFieldRequired;
  }

  @Override
  public final List<Field> getProperties() {
    return Collections.singletonList(this);
  }

  @Override
  public final Field cloneField(final String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }
}
