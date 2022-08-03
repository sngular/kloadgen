/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.config.schemaregistry;

import java.beans.PropertyDescriptor;

import net.coru.kloadgen.model.PropertyMapping;
import net.coru.kloadgen.property.editor.SchemaRegistryConfigPropertyEditor;
import net.coru.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class SchemaRegistryConfigElementBeanInfo extends BeanInfoSupport {

  private static final String SCHEMA_REGISTRY_URL = "schemaRegistryUrl";

  private static final String SCHEMA_REGISTRY_PROPERTIES = "schemaRegistryProperties";

  public SchemaRegistryConfigElementBeanInfo() {

    super(SchemaRegistryConfigElement.class);

    createPropertyGroup("schema_registry_config", new String[]{SCHEMA_REGISTRY_URL, SCHEMA_REGISTRY_PROPERTIES});

    final PropertyDescriptor schemaRegistryUrl = property(SCHEMA_REGISTRY_URL);
    schemaRegistryUrl.setPropertyEditorClass(SchemaRegistryConfigPropertyEditor.class);
    schemaRegistryUrl.setValue(NOT_UNDEFINED, Boolean.TRUE);
    schemaRegistryUrl.setValue(DEFAULT, SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL_DEFAULT);
    schemaRegistryUrl.setValue(NOT_EXPRESSION, Boolean.FALSE);

    final TypeEditor tableEditor = TypeEditor.TableEditor;
    final PropertyDescriptor tableProperties = property(SCHEMA_REGISTRY_PROPERTIES, tableEditor);
    tableProperties.setValue(TableEditor.CLASSNAME, PropertyMapping.class.getName());
    tableProperties.setValue(TableEditor.HEADERS, new String[]{"Property Name", "Property Value"});
    tableProperties.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{PropertyMapping.PROPERTY_NAME, PropertyMapping.PROPERTY_VALUE});
    tableProperties.setValue(DEFAULT, DefaultPropertiesHelper.DEFAULTS);
    tableProperties.setValue(NOT_UNDEFINED, Boolean.TRUE);
    tableProperties.setValue(NOT_EXPRESSION, Boolean.FALSE);

  }
}
