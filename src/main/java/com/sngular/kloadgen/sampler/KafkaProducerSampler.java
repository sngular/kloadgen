/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.sampler;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.loadgen.BaseLoadGenerator;
import com.sngular.kloadgen.model.HeaderMapping;
import com.sngular.kloadgen.randomtool.generator.StatelessGeneratorTool;
import com.sngular.kloadgen.serializer.AvroSerializer;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.serializer.ProtobufSerializer;
import com.sngular.kloadgen.util.ProducerKeysHelper;
import com.sngular.kloadgen.util.PropsKeysHelper;
import io.apicurio.registry.serde.SerdeConfig;
import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;

public final class KafkaProducerSampler extends AbstractJavaSamplerClient implements Serializable {

  private static final String TEMPLATE = "Topic: %s, partition: %s, offset: %s";

  private static final Set<String> SERIALIZER_SET = Set.of(AvroSerializer.class.getName(), ProtobufSerializer.class.getName());

  private static final long serialVersionUID = 1L;

  private final transient StatelessGeneratorTool statelessGeneratorTool = new StatelessGeneratorTool();

  private transient KafkaProducer<Object, Object> producer;

  private String topic;

  private String msgKeyType;

  private List<String> msgKeyValue;

  private boolean keyMessageFlag = false;

  private transient BaseLoadGenerator generator;

  private transient BaseLoadGenerator keyGenerator;

  private transient Properties props;

  @Override
  public void setupTest(final JavaSamplerContext context) {
    props = properties(context);

    generator = SamplerUtil.configureValueGenerator(props);

    if ("true".equals(context.getJMeterVariables().get(PropsKeysHelper.SCHEMA_KEYED_MESSAGE_KEY))
        || "true".equals(context.getJMeterVariables().get(PropsKeysHelper.SIMPLE_KEYED_MESSAGE_KEY))) {
      keyMessageFlag = true;
      if (!Objects.isNull(JMeterContextService.getContext().getVariables().get(PropsKeysHelper.KEY_SUBJECT_NAME))) {
        keyGenerator = SamplerUtil.configureKeyGenerator(props);
      } else {
        msgKeyType = props.getProperty(PropsKeysHelper.MESSAGE_KEY_KEY_TYPE);
        msgKeyValue = PropsKeysHelper.MSG_KEY_VALUE.equalsIgnoreCase(props.getProperty(PropsKeysHelper.MESSAGE_KEY_KEY_VALUE))
                          ? Collections.emptyList() : Collections.singletonList(props.getProperty(PropsKeysHelper.MESSAGE_KEY_KEY_VALUE));
      }
    } else {
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ProducerKeysHelper.KEY_SERIALIZER_CLASS_CONFIG_DEFAULT);
    }

    topic = context.getParameter(ProducerKeysHelper.KAFKA_TOPIC_CONFIG);
    try {

      Properties props2 = new Properties();

      props2.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
      props2.put(ProducerConfig.CLIENT_ID_CONFIG, context.getParameter(ProducerConfig.CLIENT_ID_CONFIG));
      props2.putIfAbsent(ProducerConfig.ACKS_CONFIG, "all");
      props2.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      // Use the Apicurio Registry provided Kafka Serializer for Avro
      props2.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroKafkaSerializer.class.getName());

      // Configure Service Registry location
      props2.putIfAbsent(SerdeConfig.REGISTRY_URL, "http://localhost:8080");

      // Register the artifact if not found in the registry.
      // props2.putIfAbsent(SerdeConfig.AUTO_REGISTER_ARTIFACT, Boolean.TRUE);

      props2.putIfAbsent("apicurio.registry.artifact-resolver-strategy", props.get(ProducerKeysHelper.VALUE_NAME_STRATEGY));

      producer = new KafkaProducer<>(props2);
    } catch (final KafkaException ex) {
      getNewLogger().error(ex.getMessage(), ex);
    }
  }

  private Properties properties(final JavaSamplerContext context) {
    final var commonProps = SamplerUtil.setupCommonProperties(context);
    if (Objects.nonNull(context.getParameter(ProducerKeysHelper.VALUE_NAME_STRATEGY))) {
      commonProps.put(ProducerKeysHelper.VALUE_NAME_STRATEGY, context.getParameter(ProducerKeysHelper.VALUE_NAME_STRATEGY));
    }
    return commonProps;
  }

  @Override
  public void teardownTest(final JavaSamplerContext context) {
    if (Objects.nonNull(producer)) {
      producer.close();
    }
  }

  @Override
  public Arguments getDefaultParameters() {
    return SamplerUtil.getCommonDefaultParameters();
  }

  @Override
  public SampleResult runTest(final JavaSamplerContext javaSamplerContext) {

    final var sampleResult = new SampleResult();
    sampleResult.sampleStart();
    final var jMeterContext = JMeterContextService.getContext();
    final var messageVal = generator.nextMessage();
    final var kafkaHeaders = safeGetKafkaHeaders(jMeterContext);

    if (Objects.nonNull(messageVal)) {

      try {
        final var producerRecord = getProducerRecord(messageVal, enrichedKeyFlag(), enrichedValueFlag());
        final var headersSB = new ArrayList<>(SamplerUtil.populateHeaders(kafkaHeaders, producerRecord));

        sampleResult.setRequestHeaders(StringUtils.join(headersSB, ","));
        fillSamplerResult(producerRecord, sampleResult);

        final var result = producer.send(producerRecord, (metadata, e) -> {
          if (e != null) {
            super.getNewLogger().error("Send failed for record {}", producerRecord, e);
            throw new KLoadGenException("Failed to sent message due ", e);
          }
        });

        fillSampleResult(sampleResult, prettyPrint(result.get()), true);
      } catch (KLoadGenException | InterruptedException | ExecutionException e) {
        super.getNewLogger().error("Failed to send message", e);
        fillSampleResult(sampleResult, e.getMessage() != null ? e.getMessage() : "", false);
      }
    } else {
      super.getNewLogger().error("Failed to Generate message");
      fillSampleResult(sampleResult, "Failed to Generate message", false);
    }
    return sampleResult;
  }

  private List<HeaderMapping> safeGetKafkaHeaders(final JMeterContext jmeterContext) {
    final var headerMappingList = new ArrayList<HeaderMapping>();
    final var headers = jmeterContext.getSamplerContext().get(ProducerKeysHelper.KAFKA_HEADERS);
    if (null != headers) {
      headerMappingList.addAll((List) headers);
    }
    return headerMappingList;
  }

  private ProducerRecord<Object, Object> getProducerRecord(final EnrichedRecord messageVal, final boolean keyFlag, final boolean valueFlag) {
    final ProducerRecord<Object, Object> producerRecord;
    if (keyMessageFlag) {
      if (Objects.isNull(keyGenerator)) {
        final var key = statelessGeneratorTool.generateObject("key", msgKeyType, 0, msgKeyValue).toString();
        producerRecord = new ProducerRecord<>(topic, key, getObject(messageVal, valueFlag));
      } else {
        final var key = keyGenerator.nextMessage();
        producerRecord = new ProducerRecord<>(topic, getObject(key, keyFlag), getObject(messageVal, valueFlag));
      }
    } else {
      producerRecord = new ProducerRecord<>(topic, getObject(messageVal, valueFlag));
    }
    return producerRecord;
  }

  private Boolean enrichedKeyFlag() {
    return SERIALIZER_SET.contains(props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG).toString());
  }

  private Boolean enrichedValueFlag() {
    return SERIALIZER_SET.contains(props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG).toString());
  }

  private void fillSamplerResult(final ProducerRecord<Object, Object> producerRecord, final SampleResult sampleResult) {
    if (Objects.isNull(producerRecord.key())) {
      sampleResult.setSamplerData(String.format("key: null, payload: %s", producerRecord.value().toString()));
    } else {
      sampleResult.setSamplerData(String.format("key: %s, payload: %s", producerRecord.key().toString(),
                                                producerRecord.value().toString()));
    }
  }

  private void fillSampleResult(final SampleResult sampleResult, final String respondeData, final boolean successful) {
    sampleResult.setResponseData(respondeData, StandardCharsets.UTF_8.name());
    sampleResult.setSuccessful(successful);
    sampleResult.sampleEnd();
  }

  private String prettyPrint(final RecordMetadata recordMetadata) {
    return String.format(TEMPLATE, recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
  }

  private Object getObject(final EnrichedRecord messageVal, final boolean valueFlag) {
    return valueFlag ? messageVal : messageVal.getGenericRecord();
  }
}
