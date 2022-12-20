/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.randomtool.random;

import java.util.function.Supplier;

class IteratorType<T> {

  private final Supplier<T> getDefaultForType;

  public IteratorType(final Supplier<T> getDefaultForType) {
    this.getDefaultForType = getDefaultForType;
  }

  public static <T> IteratorType<T> of(final Supplier<T> getDefaultForType) {
    return new IteratorType<T>(getDefaultForType);
  }

  Object getDefaultForType() {
    return getDefaultForType.get();
  }
}
