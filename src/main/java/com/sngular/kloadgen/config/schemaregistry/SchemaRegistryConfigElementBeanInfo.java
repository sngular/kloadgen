/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.config.schemaregistry;

import java.beans.PropertyDescriptor;

import com.sngular.kloadgen.model.PropertyMapping;
import com.sngular.kloadgen.property.editor.SchemaRegistryConfigPropertyEditor;
import com.sngular.kloadgen.property.editor.SchemaRegistryNamePropertyEditor;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class SchemaRegistryConfigElementBeanInfo extends BeanInfoSupport {

  public SchemaRegistryConfigElementBeanInfo() {

    super(SchemaRegistryConfigElement.class);

    createPropertyGroup("schema_registry_config", new String[] {SchemaRegistryConfigElementValue.SCHEMA_REGISTRY_NAME,
                                                                SchemaRegistryConfigElementValue.SCHEMA_REGISTRY_URL,
                                                                SchemaRegistryConfigElementValue.SCHEMA_REGISTRY_PROPERTIES});

    final PropertyDescriptor schemaRegistryName = property(SchemaRegistryConfigElementValue.SCHEMA_REGISTRY_NAME);
    schemaRegistryName.setPropertyEditorClass(SchemaRegistryNamePropertyEditor.class);
    schemaRegistryName.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaRegistryName.setValue(DEFAULT, SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME_DEFAULT);
    schemaRegistryName.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final PropertyDescriptor schemaRegistryUrl = property(SchemaRegistryConfigElementValue.SCHEMA_REGISTRY_URL);
    schemaRegistryUrl.setPropertyEditorClass(SchemaRegistryConfigPropertyEditor.class);
    schemaRegistryUrl.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaRegistryUrl.setValue(DEFAULT, SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL_DEFAULT);
    schemaRegistryUrl.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final TypeEditor tableEditor = TypeEditor.TableEditor;
    final PropertyDescriptor tableProperties = property(SchemaRegistryConfigElementValue.SCHEMA_REGISTRY_PROPERTIES, tableEditor);
    tableProperties.setValue(TableEditor.CLASSNAME, PropertyMapping.class.getName());
    tableProperties.setValue(TableEditor.HEADERS, new String[]{"Property Name", "Property Value"});
    tableProperties.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{PropertyMapping.PROPERTY_NAME, PropertyMapping.PROPERTY_VALUE});
    tableProperties.setValue(DEFAULT, DefaultPropertiesHelper.DEFAULTS);
    tableProperties.setValue(NOT_UNDEFINED, Boolean.TRUE);
    tableProperties.setValue(NOT_EXPRESSION, Boolean.FALSE);

  }
}
