/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.strategy;

import java.util.Map;
import java.util.Objects;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.common.errors.SerializationException;

public class RecordSubjectNameStrategy implements io.confluent.kafka.serializers.subject.strategy.SubjectNameStrategy {

  @Deprecated(forRemoval = true)
  public final String getSubjectName(final String topic, final boolean isKey, final Object value) {
    return subjectName(topic, isKey, new AvroSchema(AvroSchemaUtils.getSchema(value)));
  }

  @Override
  public final String subjectName(final String topic, final boolean isKey, final ParsedSchema parsedSchema) {
    String result = null;
    if (parsedSchema != null) {
      result = getRecordName(parsedSchema, isKey);
    }
    return result;
  }

  private String getRecordName(final ParsedSchema schema, final boolean isKey) {
    final String name = ((AvroSchema) schema).rawSchema().getObjectProps().get("subject").toString();
    if (Objects.isNull(name)) {
      if (isKey) {
        throwTheException(AbstractKafkaSchemaSerDeConfig.KEY_SUBJECT_NAME_STRATEGY, "key");
      } else {
        throwTheException(AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, "value");
      }
    }
    return name;
  }

  private void throwTheException(final String configuration, final String type) {
    throw new SerializationException("In configuration "
                                     + configuration + " = "
                                     + getClass().getName() + ", the message " + type + " must only be a record schema");
  }

  @Override
  public final void configure(final Map<String, ?> configs) {
    // No configuration required.
  }

}
