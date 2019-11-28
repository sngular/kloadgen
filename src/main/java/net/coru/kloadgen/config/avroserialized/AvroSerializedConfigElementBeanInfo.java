package net.coru.kloadgen.config.avroserialized;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import net.coru.kloadgen.input.avro.AvroSchemaPropertyEditor;
import net.coru.kloadgen.input.avro.AvroSubjectPropertyEditor;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.ProducerKeys;
import net.coru.kloadgen.util.PropsKeys;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class AvroSerializedConfigElementBeanInfo extends BeanInfoSupport {

    private static final String AVRO_SUBJECT = "avroSubject";

    private static final String SCHEMA_REGISTRY_URL = "schemaRegistryUrl";

    private static final String SCHEMA_PROPERTIES = "schemaProperties";

    /**
     * Constructor which creates property group and creates UI for SerializedConfigElement.
     */
    public AvroSerializedConfigElementBeanInfo() {

        super(AvroSerializedConfigElement.class);

        //Create Property group
        createPropertyGroup("avro_serialized_load_generator", new String[] {
            SCHEMA_REGISTRY_URL, AVRO_SUBJECT, SCHEMA_PROPERTIES
        });

        //Create table editor component of jmeter for class field and expression mapping
        TypeEditor tableEditor = TypeEditor.TableEditor;
        PropertyDescriptor tableProperties = property(SCHEMA_PROPERTIES, tableEditor);
        tableProperties.setValue(TableEditor.CLASSNAME, FieldValueMapping.class.getName());
        tableProperties.setValue(TableEditor.HEADERS, new String[]{ "Field Name", "Field Expression" } );
        tableProperties.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{ FieldValueMapping.FIELD_NAME, FieldValueMapping.VALUE_EXPRESSION} );
        tableProperties.setValue(DEFAULT, new ArrayList<>());
        tableProperties.setValue(NOT_UNDEFINED, Boolean.TRUE);

        //Create class name input textfield
        PropertyDescriptor subjectNameProps = property(AVRO_SUBJECT);
        subjectNameProps.setPropertyEditorClass(AvroSubjectPropertyEditor.class);
        subjectNameProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
        subjectNameProps.setValue(DEFAULT, "<avro subject>");
        subjectNameProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

        PropertyDescriptor schemaRegistryUrl = property(SCHEMA_REGISTRY_URL);
        schemaRegistryUrl.setPropertyEditorClass(AvroSchemaPropertyEditor.class);
        schemaRegistryUrl.setValue(NOT_UNDEFINED, Boolean.TRUE);
        schemaRegistryUrl.setValue(DEFAULT, ProducerKeys.SCHEMA_REGISTRY_URL_DEFAULT);
        schemaRegistryUrl.setValue(NOT_EXPRESSION, Boolean.FALSE);

    }
}
