/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.parsedschema;

import lombok.Getter;

@Getter
public abstract class AbstractParsedSchema<T> {

  private final T schema;
  private final String schemaType;
  private final String name;

  public AbstractParsedSchema(final String name, final String type, final T schema) {
    this.name = name;
    this.schemaType = type;
    this.schema = schema;
  }

  public final T schema() {
    return this.schema;
  }

  public final String schemaType() {
    return this.schemaType;
  }

  public abstract T getRawSchema();
}
