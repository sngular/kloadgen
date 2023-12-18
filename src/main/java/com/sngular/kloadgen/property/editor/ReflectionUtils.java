/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.property.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.reflections.Reflections;

public final class ReflectionUtils {

  private ReflectionUtils() {
  }

  public static List<String> extractSerializers(final Reflections reflections, final Class reflectedClass) {
    final Set<Class<? extends Serializer>> subTypes = reflections.getSubTypesOf(reflectedClass);
    final List<String> classList = new ArrayList<>();

    for (final Class serializer : subTypes) {
      classList.add(serializer.getName());
    }

    classList.sort(Comparator.naturalOrder());
    return classList;
  }

  static void extractSerializers(final JComboBox<String> serializerComboBox, final Reflections reflections, final Class reflectedClass) {
    for (String serializer : extractSerializers(reflections, reflectedClass)) {
      serializerComboBox.addItem(serializer);
    }
    serializerComboBox.setSelectedItem(0);
  }

  static void extractDeserializers(final JComboBox<String> deserializerComboBox, final Reflections reflections, final Class subTypeClass) {
    final Set<Class<? extends Deserializer>> subTypes = reflections.getSubTypesOf(subTypeClass);
    final List<String> classList = new ArrayList<>();

    for (final Class deserializer : subTypes) {
      classList.add(deserializer.getName());
    }

    classList.sort(Comparator.naturalOrder());
    for (String deserializer : classList) {
      deserializerComboBox.addItem(deserializer);
    }
    deserializerComboBox.setSelectedItem(0);
  }
}
