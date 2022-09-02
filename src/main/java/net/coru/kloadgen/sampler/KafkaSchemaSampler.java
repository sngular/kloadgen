/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.loadgen.BaseLoadGenerator;
import net.coru.kloadgen.model.HeaderMapping;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;
import net.coru.kloadgen.serializer.AvroSerializer;
import net.coru.kloadgen.serializer.EnrichedRecord;
import net.coru.kloadgen.serializer.ProtobufSerializer;
import net.coru.kloadgen.util.ProducerKeysHelper;
import net.coru.kloadgen.util.PropsKeysHelper;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.apache.avro.Conversions;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;
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
import org.json.JSONException;
import org.json.JSONObject;

public final class KafkaSchemaSampler extends AbstractJavaSamplerClient implements Serializable {

  private static final String TEMPLATE = "Topic: %s, partition: %s, offset: %s";
  private static final Set<String> SERIALIZER_SET = Set.of(AvroSerializer.class.getName(), ProtobufSerializer.class.getName());
  private static final long serialVersionUID = 1L;
  private final transient StatelessGeneratorTool statelessGeneratorTool = new StatelessGeneratorTool();
  private transient KafkaProducer<Object, Object> producer;
  private String topic;
  private String msgKeyType;
  private List<String> msgKeyValue;
  private List<String> msgValue;
  private boolean keyMessageFlag = false;
  private transient BaseLoadGenerator generator;
  private transient BaseLoadGenerator keyGenerator;
  private transient Properties props;

  @Override
  public void setupTest(final JavaSamplerContext context) {
    props = properties(context);

    if (!Objects.isNull(JMeterContextService.getContext().getVariables().get(PropsKeysHelper.VALUE_SUBJECT_NAME))) {
      generator = SamplerUtil.configureValueGenerator(props);
    } else {
      msgValue = Collections.singletonList(props.getProperty(PropsKeysHelper.MESSAGE_KEY_VALUE));
    }

    configGenericData();

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
      producer = new KafkaProducer<>(props);
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

  private void configGenericData() {
    final var genericData = GenericData.get();

    genericData.addLogicalTypeConversion(new TimeConversions.DateConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.LocalTimestampMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.LocalTimestampMillisConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimeMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimeMillisConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimestampMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
    genericData.addLogicalTypeConversion(new Conversions.DecimalConversion());
    genericData.addLogicalTypeConversion(new Conversions.UUIDConversion());
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
    final EnrichedRecord messageVal;
    if (null != generator) {
      messageVal = generator.nextMessage();
    } else {
      final String valueStrTmp = statelessGeneratorTool.generateObject("value", "string", 0, msgValue).toString();
      final String valueStr;
      try {
        JSONObject jsonObject = new JSONObject(valueStrTmp);
        valueStr = jsonObject.toString();
      } catch (JSONException e) {
        super.getNewLogger().error("Invalid JSON input value: " + e);
        throw new KLoadGenException(e);
      }
      messageVal = EnrichedRecord.builder().genericRecord(valueStr).build();
    }

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

        super.getNewLogger().info("Send message with key: {} and body: {} and headers: {}",
                                  producerRecord.key(), producerRecord.value(), producerRecord.headers());
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
