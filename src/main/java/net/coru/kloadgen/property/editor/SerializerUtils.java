package net.coru.kloadgen.property.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import org.apache.kafka.common.serialization.Serializer;
import org.reflections.Reflections;

class SerializerUtils {

  protected static void extractSerializers(JComboBox<String> serializerComboBox, Reflections reflections) {
    Set<Class<? extends Serializer>> subTypes = reflections.getSubTypesOf(Serializer.class);
    List<String> classList = new ArrayList<>();

    for (Class serializer : subTypes) {
      classList.add(serializer.getName());
    }

    classList.sort(Comparator.naturalOrder());
    for (String serializer : classList) {
      serializerComboBox.addItem(serializer);
    }
    serializerComboBox.setSelectedItem(0);
  }
}
