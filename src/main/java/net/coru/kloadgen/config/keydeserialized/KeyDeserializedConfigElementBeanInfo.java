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

package net.coru.kloadgen.config.keydeserialized;

import java.beans.PropertyDescriptor;
import net.coru.kloadgen.property.editor.KeyDeserializerPropertyEditor;
import net.coru.kloadgen.property.editor.NameStrategyPropertyEditor;
import net.coru.kloadgen.property.editor.SchemaTypePropertyEditor;
import org.apache.jmeter.testbeans.BeanInfoSupport;

public class KeyDeserializedConfigElementBeanInfo extends BeanInfoSupport {

  private static final String KEY_SCHEMA_PROPERTIES = "keySchemaProperties";

  private static final String KEY_SCHEMA_TYPE = "keySchemaType";

  private static final String KEY_DESERIALIZER_PROPERTY = "keyDeserializerConfiguration";

  private static final String KEY_NAME_STRATEGY = "keyNameStrategy";

  public KeyDeserializedConfigElementBeanInfo() {

    super(KeyDeserializedConfigElement.class);

    createPropertyGroup("key_deserialized_load_generator", new String[]{
        KEY_NAME_STRATEGY, KEY_DESERIALIZER_PROPERTY, /*KEY_SUBJECT_NAME,*/ KEY_SCHEMA_PROPERTIES, KEY_SCHEMA_TYPE
    });

    PropertyDescriptor nameStrategyPropertyProps = property(KEY_NAME_STRATEGY);
    nameStrategyPropertyProps.setPropertyEditorClass(NameStrategyPropertyEditor.class);
    nameStrategyPropertyProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    nameStrategyPropertyProps.setValue(DEFAULT, "");
    nameStrategyPropertyProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    PropertyDescriptor serializerPropertyProps = property(KEY_DESERIALIZER_PROPERTY);
    serializerPropertyProps.setPropertyEditorClass(KeyDeserializerPropertyEditor.class);
    serializerPropertyProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    serializerPropertyProps.setValue(DEFAULT, "");
    serializerPropertyProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    PropertyDescriptor schemaType = property(KEY_SCHEMA_TYPE);
    schemaType.setPropertyEditorClass(SchemaTypePropertyEditor.class);
    schemaType.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaType.setValue(DEFAULT, "<avro subject>");
    schemaType.setValue(NOT_EXPRESSION, Boolean.FALSE);
  }
}
