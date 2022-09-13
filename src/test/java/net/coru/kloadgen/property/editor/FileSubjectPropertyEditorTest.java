/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.property.editor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.testutil.FileHelper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileSubjectPropertyEditorTest {

  private final FileHelper fileHelper = new FileHelper();

  private final FileSubjectPropertyEditor editor = new FileSubjectPropertyEditor();

  private final SchemaExtractor extractor = new SchemaExtractorImpl();

  @Test
  @DisplayName("File Subject Property Editor extract AVRO")
  void extractEmbeddedAvroTest() throws IOException {
    final File testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
    final ParsedSchema schema = extractor.schemaTypesList(testFile, "AVRO");
    final List<FieldValueMapping> fieldValueMappingList = editor.getAttributeList(schema);

    Assertions.assertThat(fieldValueMappingList)
              .hasSize(4)
              .containsExactlyInAnyOrder(
                  FieldValueMapping.builder().fieldName("fieldMySchema.testInt_id").fieldType("int").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("fieldMySchema.testLong").fieldType("long").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("fieldMySchema.fieldString").fieldType("string").required(true).isAncestorRequired(true).build(),
                  FieldValueMapping.builder().fieldName("timestamp").fieldType("long").required(true).isAncestorRequired(true).build());
  }
}