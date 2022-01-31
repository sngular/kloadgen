/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.keysimple;

import static net.coru.kloadgen.util.PropsKeysHelper.MSG_KEY_TYPE;

import java.beans.PropertyDescriptor;

import net.coru.kloadgen.property.editor.PlainKeySerializerPropertyEditor;
import org.apache.jmeter.testbeans.BeanInfoSupport;

public class KeySimpleConfigElementBeanInfo extends BeanInfoSupport {

  private static final String KEY_VALUE = "keyValue";

  private static final String KEY_TYPE = "keyType";

  private static final String KEY_SERIALIZER_PROPERTY = "keySerializerConfiguration";

  public KeySimpleConfigElementBeanInfo() {

    super(KeySimpleConfigElement.class);

    createPropertyGroup("key_simple_configuration", new String[]{
        KEY_VALUE, KEY_TYPE, KEY_SERIALIZER_PROPERTY
    });

    PropertyDescriptor keyValueProp = property(KEY_VALUE);
    keyValueProp.setValue(DEFAULT, "");
    keyValueProp.setValue(NOT_UNDEFINED, Boolean.TRUE);
    keyValueProp.setValue(NOT_EXPRESSION, Boolean.FALSE);

    PropertyDescriptor keyTypeProp = property(KEY_TYPE);
    keyTypeProp.setValue(DEFAULT, MSG_KEY_TYPE);
    keyTypeProp.setValue(NOT_UNDEFINED, Boolean.TRUE);
    keyTypeProp.setValue(NOT_EXPRESSION, Boolean.FALSE);

    PropertyDescriptor schemaType = property(KEY_SERIALIZER_PROPERTY);
    schemaType.setPropertyEditorClass(PlainKeySerializerPropertyEditor.class);
    schemaType.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaType.setValue(DEFAULT, "<avro subject>");
    schemaType.setValue(NOT_EXPRESSION, Boolean.FALSE);
  }
}
