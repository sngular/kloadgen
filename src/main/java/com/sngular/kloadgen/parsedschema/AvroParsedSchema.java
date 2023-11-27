/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.parsedschema;

import org.apache.avro.Schema;

public class AvroParsedSchema extends AbstractParsedSchema<Schema> {

  public AvroParsedSchema(final String name, final Schema schema) {
    super(name, "AVRO", schema);
  }

  @Override
  public final Schema getRawSchema() {
    return getSchema();
  }
}
