package net.coru.kloadgen.config.schemaregistry;

import net.coru.kloadgen.input.avro.SchemaRegistryConfigPropertyEditor;
import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

import static net.coru.kloadgen.util.SchemaRegistryKeys.SCHEMA_REGISTRY_CONFIG_DEFAULT;

public class SchemaRegistryConfigElementBeanInfo extends BeanInfoSupport {

    private static final String SCHEMA_REGISTRY_CONFIG = "schemaRegistryConfig";

    public SchemaRegistryConfigElementBeanInfo() {

        super(SchemaRegistryConfigElement.class);

        createPropertyGroup("schema_registry_config", new String[] {
                SCHEMA_REGISTRY_CONFIG
        });

        PropertyDescriptor schemaRegistryUrl = property(SCHEMA_REGISTRY_CONFIG);
        schemaRegistryUrl.setPropertyEditorClass(SchemaRegistryConfigPropertyEditor.class);
        schemaRegistryUrl.setValue(NOT_UNDEFINED, Boolean.TRUE);
        schemaRegistryUrl.setValue(DEFAULT, SCHEMA_REGISTRY_CONFIG_DEFAULT);
        schemaRegistryUrl.setValue(NOT_EXPRESSION, Boolean.FALSE);

    }
}
