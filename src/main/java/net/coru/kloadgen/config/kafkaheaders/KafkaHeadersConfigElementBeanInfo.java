package net.coru.kloadgen.config.kafkaheaders;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import net.coru.kloadgen.model.HeaderMapping;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class KafkaHeadersConfigElementBeanInfo extends BeanInfoSupport {

    private static final String KAFKA_HEADERS = "kafkaHeaders";

    public KafkaHeadersConfigElementBeanInfo() {

        super(KafkaHeadersConfigElement.class);

        //Create Property group
        createPropertyGroup("kafka_headers", new String[] { KAFKA_HEADERS
        });

        //Create table editor component of jmeter for class field and expression mapping
        TypeEditor tableEditor = TypeEditor.TableEditor;
        PropertyDescriptor tableProperties = property(KAFKA_HEADERS, tableEditor);
        tableProperties.setValue(TableEditor.CLASSNAME, HeaderMapping.class.getName());
        tableProperties.setValue(TableEditor.HEADERS, new String[]{ "Header Name", "Header Value" } );
        tableProperties.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{ HeaderMapping.HEADER_NAME, HeaderMapping.HEADER_VALUE} );
        tableProperties.setValue(DEFAULT, new ArrayList<>());
        tableProperties.setValue(NOT_UNDEFINED, Boolean.TRUE);
    }
}
