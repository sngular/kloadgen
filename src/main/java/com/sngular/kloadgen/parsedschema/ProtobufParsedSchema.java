/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.parsedschema;

import com.squareup.wire.schema.internal.parser.ProtoFileElement;

public class ProtobufParsedSchema extends AbstractParsedSchema<ProtoFileElement> {

  public ProtobufParsedSchema(final String name, final ProtoFileElement schema) {
    super(name, "PROTOBUF", schema);
  }

  @Override
  public final ProtoFileElement getRawSchema() {
    return getSchema();
  }
}
