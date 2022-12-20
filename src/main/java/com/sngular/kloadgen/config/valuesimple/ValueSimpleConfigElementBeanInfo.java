/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.valuesimple;

import java.beans.PropertyDescriptor;

import com.sngular.kloadgen.property.editor.PlainValueSerializerPropertyEditor;
import com.sngular.kloadgen.util.PropsKeysHelper;
import org.apache.jmeter.testbeans.BeanInfoSupport;

public class ValueSimpleConfigElementBeanInfo extends BeanInfoSupport {

  private static final String MESSAGE_TYPE = "messageType";

  private static final String MESSAGE_VALUE = "messageValue";

  private static final String MESSAGE_SERIALIZER_PROPERTY = "messageSerializerProperty";

  public ValueSimpleConfigElementBeanInfo() {

    super(ValueSimpleConfigElement.class);

    createPropertyGroup("value_simple_configuration", new String[]{MESSAGE_VALUE, MESSAGE_TYPE, MESSAGE_SERIALIZER_PROPERTY});

    final PropertyDescriptor keyValueProp = property(MESSAGE_VALUE);
    keyValueProp.setValue(DEFAULT, "");
    keyValueProp.setValue(NOT_UNDEFINED, Boolean.TRUE);
    keyValueProp.setValue(NOT_EXPRESSION, Boolean.TRUE);

    final PropertyDescriptor keyTypeProp = property(MESSAGE_TYPE);
    keyTypeProp.setValue(DEFAULT, PropsKeysHelper.MSG_KEY_TYPE);
    keyTypeProp.setValue(NOT_UNDEFINED, Boolean.TRUE);
    keyTypeProp.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor schemaType = property(MESSAGE_SERIALIZER_PROPERTY);
    schemaType.setPropertyEditorClass(PlainValueSerializerPropertyEditor.class);
    schemaType.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaType.setValue(DEFAULT, "org.apache.kafka.common.serialization.StringSerializer");
    schemaType.setValue(NOT_EXPRESSION, Boolean.FALSE);

  }
}
