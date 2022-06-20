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

package net.coru.kloadgen.config.valuedeserialized;

import java.beans.PropertyDescriptor;

import net.coru.kloadgen.property.editor.NameStrategyPropertyEditor;
import net.coru.kloadgen.property.editor.SchemaTypePropertyEditor;
import net.coru.kloadgen.property.editor.ValueDeserializerPropertyEditor;
import org.apache.jmeter.testbeans.BeanInfoSupport;

public class ValueDeserializedConfigElementBeanInfo extends BeanInfoSupport {

  private static final String VALUE_SCHEMA_PROPERTIES = "valueSchemaProperties";

  private static final String VALUE_SCHEMA_TYPE = "valueSchemaType";

  private static final String VALUE_DESERIALIZER_PROPERTY = "valueDeSerializerConfiguration";

  private static final String VALUE_NAME_STRATEGY = "valueNameStrategy";

  public ValueDeserializedConfigElementBeanInfo() {

    super(ValueDeserializedConfigElement.class);

    createPropertyGroup("value_deserialized_load_generator", new String[]{
      VALUE_NAME_STRATEGY, VALUE_DESERIALIZER_PROPERTY, VALUE_SCHEMA_PROPERTIES, VALUE_SCHEMA_TYPE
    });

    final PropertyDescriptor nameStrategyPropertyProps = property(VALUE_NAME_STRATEGY);
    nameStrategyPropertyProps.setPropertyEditorClass(NameStrategyPropertyEditor.class);
    nameStrategyPropertyProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    nameStrategyPropertyProps.setValue(DEFAULT, "");
    nameStrategyPropertyProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor serializerPropertyProps = property(VALUE_DESERIALIZER_PROPERTY);
    serializerPropertyProps.setPropertyEditorClass(ValueDeserializerPropertyEditor.class);
    serializerPropertyProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    serializerPropertyProps.setValue(DEFAULT, "");
    serializerPropertyProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor schemaType = property(VALUE_SCHEMA_TYPE);
    schemaType.setPropertyEditorClass(SchemaTypePropertyEditor.class);
    schemaType.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaType.setValue(DEFAULT, "<avro subject>");
    schemaType.setValue(NOT_EXPRESSION, Boolean.FALSE);
  }
}
