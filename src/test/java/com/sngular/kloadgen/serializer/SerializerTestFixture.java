/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.serializer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

import com.sngular.kloadgen.model.FieldValueMapping;

public final class SerializerTestFixture {

  private SerializerTestFixture() {
  }

  public static FieldValueMapping createFieldValueMapping(final String name, final String fieldType) {
    return FieldValueMapping.builder().fieldName(name).fieldType(fieldType).valueLength(0).fieldValueList("[]").required(true)
                            .isAncestorRequired(true).build();
  }

  public static FieldValueMapping createFieldValueMapping(final String name, final String fieldType, final String values) {
    return FieldValueMapping.builder().fieldName(name).fieldType(fieldType).valueLength(0).fieldValueList(values).required(true)
                            .isAncestorRequired(true).build();
  }

  public static String readSchema(final File file) throws IOException {
    final StringBuilder contentBuilder = new StringBuilder();

    try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }

    return contentBuilder.toString();
  }
}
