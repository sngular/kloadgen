package net.coru.kloadgen.config.jsonserialized;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import net.coru.kloadgen.input.avro.AvroSchemaPropertyEditor;
import net.coru.kloadgen.input.avro.AvroSubjectPropertyEditor;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.util.ProducerKeys;
import net.coru.kloadgen.util.PropsKeys;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TextAreaEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class JsonSerializedConfigElementBeanInfo extends BeanInfoSupport {

    private static final String JSON_DOCUMENT = "jsonDocument";

    private static final String SCHEMA_PROPERTIES = "schemaProperties";

    private static final String PLACE_HOLDER = "placeHolder";

    public JsonSerializedConfigElementBeanInfo() {

        super(JsonSerializedConfigElement.class);

        createPropertyGroup("avro_serialized_load_generator", new String[] {
            JSON_DOCUMENT, SCHEMA_PROPERTIES
        });

        TypeEditor tableEditor = TypeEditor.TableEditor;
        PropertyDescriptor tableProperties = property(SCHEMA_PROPERTIES, tableEditor);
        tableProperties.setValue(TableEditor.CLASSNAME, FieldValueMapping.class.getName());
        tableProperties.setValue(TableEditor.HEADERS, new String[]{ "Field Name", "Field Expression" } );
        tableProperties.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{ FieldValueMapping.FIELD_NAME, FieldValueMapping.VALUE_EXPRESSION} );
        tableProperties.setValue(DEFAULT, new ArrayList<>());
        tableProperties.setValue(NOT_UNDEFINED, Boolean.TRUE);

        //Create class name input textfield
        PropertyDescriptor subjectNameProps = property(JSON_DOCUMENT);
        subjectNameProps.setPropertyEditorClass(TextAreaEditor.class);
        subjectNameProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
        subjectNameProps.setValue(DEFAULT, "{}");
        subjectNameProps.setValue(NOT_EXPRESSION, Boolean.FALSE);

    }
}
