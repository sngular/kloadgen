/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.valuesimple;

import java.beans.PropertyDescriptor;

import net.coru.kloadgen.property.editor.PlainValueSerializerPropertyEditor;
import org.apache.jmeter.testbeans.BeanInfoSupport;

public class ValueSimpleConfigElementBeanInfo extends BeanInfoSupport {

  private static final String VALUE_SCHEMA_PROPERTIES = "valueSchemaProperties";

  private static final String VALUE_SERIALIZER_PROPERTY = "valueSerializerConfiguration";

  public ValueSimpleConfigElementBeanInfo() {

    super(ValueSimpleConfigElement.class);

    createPropertyGroup("value_simple_configuration", new String[]{VALUE_SCHEMA_PROPERTIES, VALUE_SERIALIZER_PROPERTY});

    final PropertyDescriptor keyValueProp = property(VALUE_SCHEMA_PROPERTIES);
    keyValueProp.setValue(DEFAULT, "");
    keyValueProp.setValue(NOT_UNDEFINED, Boolean.TRUE);
    keyValueProp.setValue(NOT_EXPRESSION, Boolean.TRUE);

    final PropertyDescriptor schemaType = property(VALUE_SERIALIZER_PROPERTY);
    schemaType.setPropertyEditorClass(PlainValueSerializerPropertyEditor.class);
    schemaType.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaType.setValue(DEFAULT, "org.apache.kafka.common.serialization.StringSerializer");
    schemaType.setValue(NOT_EXPRESSION, Boolean.FALSE);

  }
}
