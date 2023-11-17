/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.util;

import java.util.Properties;

import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryManagerFactory;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Slf4j
public final class JMeterHelper {

  private JMeterHelper() {
  }

  public static BaseParsedSchema getParsedSchema(final String subjectName, final Properties properties) {
    String schemaRegistryName = properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
    if (schemaRegistryName == null || StringUtils.isEmpty(schemaRegistryName)) {
      schemaRegistryName = SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME_DEFAULT;
    }
    final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry(schemaRegistryName);

    return schemaRegistryManager.getSchemaBySubject(subjectName);

  }

  public static String checkPropertyOrVariable(final String textToCheck) {
    final String result;
    if (textToCheck.matches("\\$\\{__P\\(.*\\)}")) {
      result = JMeterContextService.getContext().getProperties().getProperty(textToCheck.substring(6, textToCheck.length() - 2));
    } else if (textToCheck.matches("\\$\\{\\w*}")) {
      result = JMeterContextService.getContext().getVariables().get(textToCheck.substring(2, textToCheck.length() - 1));
    } else {
      result = textToCheck;
    }
    return result;
  }
}
