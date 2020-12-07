/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.keyserialized;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.property.editor.SchemaTypePropertyEditor;
import net.coru.kloadgen.property.editor.SerialisedSubjectPropertyEditor;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class KeySerializedConfigElementBeanInfo extends BeanInfoSupport {

    private static final String KEY_SUBJECT_NAME = "keySubjectName";

    private static final String KEY_SCHEMA_PROPERTIES = "keySchemaProperties";

    private static final String KEY_SCHEMA_TYPE = "keySchemaType";

    public KeySerializedConfigElementBeanInfo() {

        super(KeySerializedConfigElement.class);

        createPropertyGroup("key_serialized_load_generator", new String[] {
            KEY_SUBJECT_NAME, KEY_SCHEMA_PROPERTIES, KEY_SCHEMA_TYPE
        });

        TypeEditor tableEditor = TypeEditor.TableEditor;
        PropertyDescriptor tableProperties = property(KEY_SCHEMA_PROPERTIES, tableEditor);
        tableProperties.setValue(TableEditor.CLASSNAME, FieldValueMapping.class.getName());
        tableProperties.setValue(TableEditor.HEADERS,
            new String[]{
                "Field Name",
                "Field Type",
                "Field Length",
                "Field Values List"
            });
        tableProperties.setValue(TableEditor.OBJECT_PROPERTIES,
            new String[]{
                FieldValueMapping.FIELD_NAME,
                FieldValueMapping.FIELD_TYPE,
                FieldValueMapping.VALUE_LENGTH,
                FieldValueMapping.FIELD_VALUES_LIST
            });
        tableProperties.setValue(DEFAULT, new ArrayList<>());
        tableProperties.setValue(NOT_UNDEFINED, Boolean.TRUE);

        PropertyDescriptor subjectNameProps = property(KEY_SUBJECT_NAME);
        subjectNameProps.setPropertyEditorClass(SerialisedSubjectPropertyEditor.class);
        subjectNameProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
        subjectNameProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

        PropertyDescriptor schemaType = property(KEY_SCHEMA_TYPE);
        schemaType.setPropertyEditorClass(SchemaTypePropertyEditor.class);
        schemaType.setValue(NOT_UNDEFINED, Boolean.TRUE);
        schemaType.setValue(DEFAULT, "<avro subject>");
        schemaType.setValue(NOT_EXPRESSION, Boolean.FALSE);
    }
}
