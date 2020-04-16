package net.coru.kloadgen.config.fileserialized;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import net.coru.kloadgen.config.avroserialized.AvroSerializedConfigElement;
import net.coru.kloadgen.input.avro.FileSubjectPropertyEditor;
import net.coru.kloadgen.model.FieldValueMapping;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class FileSerializedConfigElementBeanInfo  extends BeanInfoSupport {

  private static final String AVRO_SUBJECT = "avroSubject";

  private static final String SCHEMA_PROPERTIES = "schemaProperties";

  public FileSerializedConfigElementBeanInfo() {

    super(FileSerializedConfigElement.class);

    createPropertyGroup("avro_serialized_load_generator", new String[] {
        AVRO_SUBJECT, SCHEMA_PROPERTIES
    });

    TypeEditor tableEditor = TypeEditor.TableEditor;
    PropertyDescriptor tableProperties = property(SCHEMA_PROPERTIES, tableEditor);
    tableProperties.setValue(TableEditor.CLASSNAME, FieldValueMapping.class.getName());
    tableProperties.setValue(TableEditor.HEADERS,
        new String[]{
            "Field Name",
            "Field Type",
            "Field Length",
            "Field Values List"
        });
    tableProperties.setValue(TableEditor.OBJECT_PROPERTIES,
        new String[]{
            FieldValueMapping.FIELD_NAME,
            FieldValueMapping.FIELD_TYPE,
            FieldValueMapping.VALUE_LENGTH,
            FieldValueMapping.FIELD_VALUES_LIST
        });
    tableProperties.setValue(DEFAULT, new ArrayList<>());
    tableProperties.setValue(NOT_UNDEFINED, Boolean.TRUE);

    PropertyDescriptor subjectNameProps = property(AVRO_SUBJECT);
    subjectNameProps.setPropertyEditorClass(FileSubjectPropertyEditor.class);
    subjectNameProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
    subjectNameProps.setValue(DEFAULT, "<avro subject>");
    subjectNameProps.setValue(NOT_EXPRESSION, Boolean.FALSE);
  }

}
