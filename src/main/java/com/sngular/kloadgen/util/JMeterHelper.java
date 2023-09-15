/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.util;

import java.util.Properties;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryAdapter;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryManagerFactory;
import com.sngular.kloadgen.schemaregistry.adapter.impl.BaseParsedSchema;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class JMeterHelper {

  private JMeterHelper() {
  }

  public static BaseParsedSchema getParsedSchema(final String subjectName, final Properties properties) {
    final String schemaRegistryName = properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);
    final SchemaRegistryAdapter schemaRegistryManager = SchemaRegistryManagerFactory.getSchemaRegistry(schemaRegistryName);
    if (schemaRegistryManager == null || StringUtils.isEmpty(schemaRegistryName)) {
      throw new KLoadGenException("Schema registry name is required");
    }
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
