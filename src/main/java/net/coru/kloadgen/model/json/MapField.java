/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.model.json;

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

  @Override
  public List<Field> getProperties() {
    return Collections.singletonList(this);
  }

  @Builder(toBuilder = true)
  public MapField(String name, Field mapType, boolean isFieldRequired) {
    super(name, "map");
    this.mapType = mapType;
    this.isFieldRequired = isFieldRequired;
  }

  @Override
  public Field cloneField(String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }
}
