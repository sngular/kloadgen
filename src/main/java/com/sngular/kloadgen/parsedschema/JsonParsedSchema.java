/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.parsedschema;

public class JsonParsedSchema extends AbstractParsedSchema<String> {

  public JsonParsedSchema(final String name, final String schema) {
    super(name, "JSON", schema);
  }

  @Override
  public final String getRawSchema() {
    return getSchema();
  }
}
