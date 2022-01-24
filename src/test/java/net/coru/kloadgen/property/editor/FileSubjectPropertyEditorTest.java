/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.property.editor;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import net.coru.kloadgen.extractor.SchemaExtractor;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.testutil.FileHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FileSubjectPropertyEditorTest {

    private final FileHelper fileHelper = new FileHelper();
    private final FileSubjectPropertyEditor editor = new FileSubjectPropertyEditor();
    private final SchemaExtractor extractor = new SchemaExtractorImpl();

    @Test
    public void extractEmbeddedAvroTest() throws IOException {
        File testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
        ParsedSchema schema = extractor.schemaTypesList(testFile, "AVRO");
        List<FieldValueMapping> fieldValueMappingList = editor.getAttributeList(schema);

        assertThat(fieldValueMappingList)
                .hasSize(4)
                .containsExactlyInAnyOrder(
                        new FieldValueMapping("fieldMySchema.testInt_id", "int"),
                        new FieldValueMapping("fieldMySchema.testLong", "long"),
                        new FieldValueMapping("fieldMySchema.fieldString", "string"),
                        new FieldValueMapping("timestamp", "long")
                );
    }
}
