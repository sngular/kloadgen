/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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

    createPropertyGroup("kafka_headers" , new String[]{KAFKA_HEADERS
    });

    TypeEditor tableEditor = TypeEditor.TableEditor;
    PropertyDescriptor tableProperties = property(KAFKA_HEADERS , tableEditor);
    tableProperties.setValue(TableEditor.CLASSNAME , HeaderMapping.class.getName());
    tableProperties.setValue(TableEditor.HEADERS , new String[]{"Header Name" , "Header Value"});
    tableProperties.setValue(TableEditor.OBJECT_PROPERTIES , new String[]{HeaderMapping.HEADER_NAME , HeaderMapping.HEADER_VALUE});
    tableProperties.setValue(DEFAULT , new ArrayList<>());
    tableProperties.setValue(NOT_UNDEFINED , Boolean.TRUE);
  }
}
