/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.valuefiledeserialized;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.property.editor.FileSubjectPropertyEditor;
import com.sngular.kloadgen.property.editor.SchemaConverterPropertyEditor;
import com.sngular.kloadgen.property.editor.SchemaTypePropertyEditor;
import com.sngular.kloadgen.property.editor.ValueDeserializerPropertyEditor;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class ValueFileDeserializedConfigElementBeanInfo extends BeanInfoSupport {

  private static final String VALUE_SUBJECT_NAME = "valueSubjectName";

  private static final String VALUE_SCHEMA_PROPERTIES = "valueSchemaProperties";

  private static final String VALUE_SCHEMA_DEFINITION = "valueSchemaDefinition";

  private static final String VALUE_SCHEMA_TYPE = "valueSchemaType";

  private static final String VALUE_DESERIALIZER_PROPERTY = "valueDeSerializerConfiguration";

  public ValueFileDeserializedConfigElementBeanInfo() {

    super(ValueFileDeserializedConfigElement.class);

    createPropertyGroup("file_deserialized_load_generator",
                        new String[]{VALUE_DESERIALIZER_PROPERTY, VALUE_SCHEMA_TYPE, VALUE_SUBJECT_NAME, VALUE_SCHEMA_PROPERTIES, VALUE_SCHEMA_DEFINITION});

    final PropertyDescriptor deserializerPropertyProps = property(VALUE_DESERIALIZER_PROPERTY);
    deserializerPropertyProps.setPropertyEditorClass(ValueDeserializerPropertyEditor.class);
    deserializerPropertyProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    deserializerPropertyProps.setValue(DEFAULT, "");
    deserializerPropertyProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor subjectNameProps = property(VALUE_SUBJECT_NAME);
    subjectNameProps.setPropertyEditorClass(FileSubjectPropertyEditor.class);
    subjectNameProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    subjectNameProps.setValue(DEFAULT, "");
    subjectNameProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor schemaType = property(VALUE_SCHEMA_TYPE);
    schemaType.setPropertyEditorClass(SchemaTypePropertyEditor.class);
    schemaType.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaType.setValue(DEFAULT, "");
    schemaType.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor avroSchemaProps = property(VALUE_SCHEMA_DEFINITION);
    avroSchemaProps.setPropertyEditorClass(SchemaConverterPropertyEditor.class);
    avroSchemaProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    avroSchemaProps.setValue(DEFAULT, "");
    avroSchemaProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final TypeEditor tableEditor = TypeEditor.TableEditor;
    final PropertyDescriptor tableProperties = property(VALUE_SCHEMA_PROPERTIES, tableEditor);
    tableProperties.setValue(TableEditor.CLASSNAME, FieldValueMapping.class.getName());
    tableProperties.setValue(TableEditor.HEADERS, new String[]{"Field Name", "Field Type"});
    tableProperties.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{FieldValueMapping.FIELD_NAME, FieldValueMapping.FIELD_TYPE});
    tableProperties.setValue(DEFAULT, new ArrayList<>());
    tableProperties.setValue(NOT_UNDEFINED, Boolean.TRUE);
  }
}
