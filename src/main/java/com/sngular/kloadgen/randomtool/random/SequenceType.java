/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.random;

import java.util.function.Function;
import java.util.function.Supplier;

class SequenceType<T> {

  private final Supplier<T> getDefaultForType;
  private final Function<Object, T> addOneCasted;

  public SequenceType(final Supplier<T> getDefaultForType, final Function<Object, T> addOneCasted) {
    this.getDefaultForType = getDefaultForType;
    this.addOneCasted = addOneCasted;
  }

  public static <T> SequenceType<T> of(final Supplier<T> getDefaultForType, final Function<Object, T> addOneCasted) {
    return new SequenceType<>(getDefaultForType, addOneCasted);
  }

  Object getDefaultForType() {
    return getDefaultForType.get();
  }

  T addOneCasted(final Object o) {
    return this.addOneCasted.apply(o);
  }
}
