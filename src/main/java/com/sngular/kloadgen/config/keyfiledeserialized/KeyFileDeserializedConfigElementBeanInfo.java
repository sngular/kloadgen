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

package com.sngular.kloadgen.config.keyfiledeserialized;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;

import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.property.editor.FileSubjectPropertyEditor;
import com.sngular.kloadgen.property.editor.KeyDeserializerPropertyEditor;
import com.sngular.kloadgen.property.editor.SchemaConverterPropertyEditor;
import com.sngular.kloadgen.property.editor.SchemaTypePropertyEditor;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class KeyFileDeserializedConfigElementBeanInfo extends BeanInfoSupport {

  private static final String KEY_SUBJECT_NAME = "keySubjectName";

  private static final String KEY_SCHEMA_PROPERTIES = "keySchemaProperties";

  private static final String KEY_SCHEMA_DEFINITION = "keySchemaDefinition";

  private static final String KEY_SCHEMA_TYPE = "keySchemaType";

  private static final String KEY_DESERIALIZER_PROPERTY = "keyDeserializerConfiguration";

  public KeyFileDeserializedConfigElementBeanInfo() {

    super(KeyFileDeserializedConfigElement.class);

    createPropertyGroup("key_deserialized_load_generator",
                        new String[]{KEY_DESERIALIZER_PROPERTY, KEY_SCHEMA_TYPE, KEY_SUBJECT_NAME, KEY_SCHEMA_DEFINITION, KEY_SCHEMA_PROPERTIES});

    final PropertyDescriptor serializerPropertyProps = property(KEY_DESERIALIZER_PROPERTY);
    serializerPropertyProps.setPropertyEditorClass(KeyDeserializerPropertyEditor.class);
    serializerPropertyProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    serializerPropertyProps.setValue(DEFAULT, "");
    serializerPropertyProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor subjectNameProps = property(KEY_SUBJECT_NAME);
    subjectNameProps.setPropertyEditorClass(FileSubjectPropertyEditor.class);
    subjectNameProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    subjectNameProps.setValue(DEFAULT, "");
    subjectNameProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor schemaType = property(KEY_SCHEMA_TYPE);
    schemaType.setPropertyEditorClass(SchemaTypePropertyEditor.class);
    schemaType.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaType.setValue(DEFAULT, "");
    schemaType.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor avroSchemaProps = property(KEY_SCHEMA_DEFINITION);
    avroSchemaProps.setPropertyEditorClass(SchemaConverterPropertyEditor.class);
    avroSchemaProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    avroSchemaProps.setValue(DEFAULT, "");
    avroSchemaProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final TypeEditor tableEditor = TypeEditor.TableEditor;
    final PropertyDescriptor tableProperties = property(KEY_SCHEMA_PROPERTIES, tableEditor);
    tableProperties.setValue(TableEditor.CLASSNAME, FieldValueMapping.class.getName());
    tableProperties.setValue(TableEditor.HEADERS, new String[]{"Field Name", "Field Type"});
    tableProperties.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{FieldValueMapping.FIELD_NAME, FieldValueMapping.FIELD_TYPE});
    tableProperties.setValue(DEFAULT, new ArrayList<>());
    tableProperties.setValue(NOT_UNDEFINED, Boolean.TRUE);
  }
}
