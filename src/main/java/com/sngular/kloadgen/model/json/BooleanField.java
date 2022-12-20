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
public class BooleanField extends Field {

  @Builder(toBuilder = true)
  public BooleanField(final String name) {
    super(name, "boolean");
  }

  @Override
  public final Field cloneField(final String fieldName) {
    return this.toBuilder().name(fieldName).build();
  }

  @Override
  public final List<Field> getProperties() {
    return Collections.singletonList(this);
  }
}
