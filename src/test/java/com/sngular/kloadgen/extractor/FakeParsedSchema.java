/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.extractor;

import com.sngular.kloadgen.parsedschema.AbstractParsedSchema;

public class FakeParsedSchema extends AbstractParsedSchema<String> {

  public FakeParsedSchema(final String name, final String type, final String schema) {
    super(name, type, schema);
  }

  @Override
  public final String getRawSchema() {
    return getSchema();
  }
}
