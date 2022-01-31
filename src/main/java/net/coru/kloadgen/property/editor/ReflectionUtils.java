package net.coru.kloadgen.property.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.reflections.Reflections;

final class ReflectionUtils {

  private ReflectionUtils() {
  }

  protected static void extractSerializers(JComboBox<String> serializerComboBox, Reflections reflections, Class reflectedClass) {
    Set<Class<? extends Serializer>> subTypes = reflections.getSubTypesOf(reflectedClass);
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

  static void extractDeserializers(javax.swing.JComboBox<String> deserializerComboBox, org.reflections.Reflections reflections) {
    Set<Class<? extends Deserializer>> subTypes = reflections.getSubTypesOf((Class) org.apache.kafka.common.serialization.Deserializer.class);
    List<String> classList = new ArrayList<>();

    for (Class deserializer : subTypes) {
      classList.add(deserializer.getName());
    }

    classList.sort(Comparator.naturalOrder());
    for (String deserializer : classList) {
      deserializerComboBox.addItem(deserializer);
    }
    deserializerComboBox.setSelectedItem(0);
  }
}
