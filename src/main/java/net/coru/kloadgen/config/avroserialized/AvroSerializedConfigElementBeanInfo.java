/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.avroserialized;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.property.editor.AvroSubjectPropertyEditor;
import net.coru.kloadgen.property.editor.SchemaTypePropertyEditor;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class AvroSerializedConfigElementBeanInfo extends BeanInfoSupport {

    private static final String SUBJECT_NAME = "subjectName";

    private static final String SCHEMA_PROPERTIES = "schemaProperties";

    private static final String SCHEMA_TYPE = "schemaType";

    public AvroSerializedConfigElementBeanInfo() {

        super(AvroSerializedConfigElement.class);

        createPropertyGroup("avro_serialized_load_generator", new String[] {
            SUBJECT_NAME, SCHEMA_PROPERTIES, SCHEMA_TYPE
        });

        TypeEditor tableEditor = TypeEditor.TableEditor;
        PropertyDescriptor tableProperties = property(SCHEMA_PROPERTIES, tableEditor);
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

        PropertyDescriptor subjectNameProps = property(SUBJECT_NAME);
        subjectNameProps.setPropertyEditorClass(AvroSubjectPropertyEditor.class);
        subjectNameProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
        subjectNameProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

        PropertyDescriptor schemaType = property(SCHEMA_TYPE);
        schemaType.setPropertyEditorClass(SchemaTypePropertyEditor.class);
        schemaType.setValue(NOT_UNDEFINED, Boolean.TRUE);
        schemaType.setValue(DEFAULT, "<avro subject>");
        schemaType.setValue(NOT_EXPRESSION, Boolean.FALSE);
    }
}
