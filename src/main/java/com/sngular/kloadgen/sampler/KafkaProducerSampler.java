/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.sngular.kloadgen.sampler;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.loadgen.BaseLoadGenerator;
import com.sngular.kloadgen.model.HeaderMapping;
import com.sngular.kloadgen.randomtool.generator.StatelessGeneratorTool;
import com.sngular.kloadgen.schemaregistry.SchemaRegistryFactory;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.util.ProducerKeysHelper;
import com.sngular.kloadgen.util.PropsKeysHelper;
import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import io.apicurio.registry.resolver.SchemaResolverConfig;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.serde.Legacy4ByteIdHandler;
import io.apicurio.registry.serde.SerdeConfig;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.Serializer;
import org.jetbrains.annotations.NotNull;

public final class KafkaProducerSampler extends AbstractJavaSamplerClient implements Serializable {

  private static final String TEMPLATE = "Topic: %s, partition: %s, offset: %s";

  @Serial
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
    props = JMeterContextService.getContext().getProperties();
    final var vars = JMeterContextService.getContext().getVariables();
    generator = SamplerUtil.configureValueGenerator(props);

    if ("true".equals(vars.get(PropsKeysHelper.SCHEMA_KEYED_MESSAGE_KEY))
        || "true".equals(vars.get(PropsKeysHelper.SIMPLE_KEYED_MESSAGE_KEY))) {
      keyMessageFlag = true;
      if (!Objects.isNull(vars.get(PropsKeysHelper.KEY_SUBJECT_NAME))) {
        keyGenerator = SamplerUtil.configureKeyGenerator(props);
      } else {
        msgKeyType = getMsgKeyType(props, vars);
        msgKeyValue = getMsgKeyValue(props, vars);
      }
    } else {
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ProducerKeysHelper.KEY_SERIALIZER_CLASS_CONFIG_DEFAULT);
    }

    if (context.getParameter(ProducerKeysHelper.APICURIO_LEGACY_ID_HANDLER).equals(ProducerKeysHelper.FLAG_YES)) {
      props.put(SerdeConfig.ID_HANDLER, Legacy4ByteIdHandler.class.getName());
    }
    if (context.getParameter(ProducerKeysHelper.APICURIO_ENABLE_HEADERS_ID).equals(ProducerKeysHelper.FLAG_NO)) {
      props.put(SerdeConfig.ENABLE_HEADERS, "false");
    }

    topic = context.getParameter(ProducerKeysHelper.KAFKA_TOPIC_CONFIG);
    try {

      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
      props.put(ProducerConfig.CLIENT_ID_CONFIG, context.getParameter(ProducerConfig.CLIENT_ID_CONFIG));
      props.putIfAbsent(ProducerConfig.ACKS_CONFIG, "all");
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, calculateKeyProperty(props, vars));
      props.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ProducerKeysHelper.VALUE_SERIALIZER_CLASS_CONFIG_DEFAULT);

      producer = new KafkaProducer<>(props,
                                     getSerializerInstance(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, props, context),
                                     getSerializerInstance(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, props, context));
    } catch (final KafkaException | ClassNotFoundException ex) {
      getNewLogger().error(ex.getMessage(), ex);
    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
      throw new KLoadGenException(e);
    }
  }

  @NotNull
  private Serializer getSerializerInstance(final String keySerializerClassConfig, final Properties props, final JavaSamplerContext context)
      throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    final String url = props.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_URL);
    final Map properties = SamplerUtil.setupSchemaRegistryAuthenticationProperties(context.getJMeterContext().getVariables());

    Serializer serializer;
    if (props.getProperty(keySerializerClassConfig).contains("apicurio")) {
      properties.putAll(getStrategyInfo(SchemaRegistryEnum.APICURIO, props));
      serializer = (Serializer) createInstance(Class.forName(props.getProperty(keySerializerClassConfig)), SchemaRegistryEnum.APICURIO, url, properties);
    } else if (props.getProperty(keySerializerClassConfig).contains("confluent")) {
      properties.putAll(getStrategyInfo(SchemaRegistryEnum.CONFLUENT, props));
      serializer = (Serializer) createInstance(Class.forName(props.getProperty(keySerializerClassConfig)), SchemaRegistryEnum.CONFLUENT, url, properties);
    } else {
      serializer = (Serializer) Class.forName(props.getProperty(keySerializerClassConfig)).getConstructor().newInstance();
    }
    return serializer;
  }

  private Map<String, ?> getStrategyInfo(final SchemaRegistryEnum schemaRegistryEnum, final Properties props) {

    return switch (schemaRegistryEnum) {
      case APICURIO -> Map.of(SchemaResolverConfig.ARTIFACT_RESOLVER_STRATEGY, props.get(SchemaResolverConfig.ARTIFACT_RESOLVER_STRATEGY),
                              "reference.subject.name.strategy", props.get(SchemaResolverConfig.ARTIFACT_RESOLVER_STRATEGY));
      case CONFLUENT -> Map.of(ProducerKeysHelper.VALUE_NAME_STRATEGY, props.get(ProducerKeysHelper.VALUE_NAME_STRATEGY),
                               "reference.subject.name.strategy", props.get(ProducerKeysHelper.VALUE_NAME_STRATEGY));
    };
  }

  private Object createInstance(final Class<?> classToGenerate, final SchemaRegistryEnum schemaRegistryEnum, final String url, final Map<String, ?> properties)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    return switch (schemaRegistryEnum) {
      case APICURIO -> classToGenerate.getConstructor(RegistryClient.class)
                             .newInstance(SchemaRegistryFactory.getSchemaRegistryClient(SchemaRegistryEnum.APICURIO, url, properties));
      case CONFLUENT -> classToGenerate.getConstructor(SchemaRegistryClient.class, Map.class)
                              .newInstance(SchemaRegistryFactory.getSchemaRegistryClient(SchemaRegistryEnum.CONFLUENT, url, properties), properties);
    };
  }

  private String calculateKeyProperty(final Properties props, final JMeterVariables vars) {
    String result = vars.get(PropsKeysHelper.KEY_SERIALIZER_CLASS_PROPERTY);
    if (Objects.isNull(result)) {
      result = props.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ProducerKeysHelper.KEY_SERIALIZER_CLASS_CONFIG_DEFAULT);
    }
    return result;
  }

  private String getMsgKeyType(final Properties props, final JMeterVariables vars) {
    String result = props.getProperty(PropsKeysHelper.MESSAGE_KEY_KEY_TYPE, null);
    if (Objects.isNull(result)) {
      result = vars.get(PropsKeysHelper.KEY_TYPE);
    }
    return result;
  }

  @NotNull
  private List<String> getMsgKeyValue(final Properties props, final JMeterVariables vars) {
    
    final List<String> result = new ArrayList<>();
    
    if (PropsKeysHelper.MSG_KEY_VALUE.equalsIgnoreCase(props.getProperty(PropsKeysHelper.MESSAGE_KEY_KEY_VALUE))
           || Objects.nonNull(props.getProperty(PropsKeysHelper.MESSAGE_KEY_KEY_VALUE))) {
      result.add(props.getProperty(PropsKeysHelper.MESSAGE_KEY_KEY_VALUE));
    } else if (Objects.nonNull(vars.get(PropsKeysHelper.KEY_VALUE))) {
      result.add(vars.get(PropsKeysHelper.KEY_VALUE));
    }
    return result;
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
    if (Objects.isNull(generator)) {
      throw new KLoadGenException("Error initializing Generator");
    }
    final var messageVal = generator.nextMessage();
    final var kafkaHeaders = safeGetKafkaHeaders(jMeterContext);

    if (Objects.nonNull(messageVal)) {
      try {
        final var producerRecord = getProducerRecord(messageVal);
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
      } catch (final KLoadGenException | InterruptedException | ExecutionException e) {
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

  private ProducerRecord<Object, Object> getProducerRecord(final EnrichedRecord messageVal) {
    final ProducerRecord<Object, Object> producerRecord;

    final String keySerializer = (String) props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
    final String valueSerializer = (String) props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);

    if (keyMessageFlag) {
      if (Objects.isNull(keyGenerator)) {
        final var key = statelessGeneratorTool.generateObject("key", msgKeyType, 0, msgKeyValue);
        producerRecord = new ProducerRecord<>(topic, key, getObject(messageVal, valueSerializer));
      } else {
        final var key = keyGenerator.nextMessage();
        producerRecord = new ProducerRecord<>(topic, getObject(key, keySerializer), getObject(messageVal, valueSerializer));
      }
    } else {
      producerRecord = new ProducerRecord<>(topic, getObject(messageVal, valueSerializer), getObject(messageVal, valueSerializer));
    }
    return producerRecord;
  }

  private void fillSamplerResult(final ProducerRecord<Object, Object> producerRecord, final SampleResult sampleResult) {
    final String result = "key: "
            + producerRecord.key()
            + ", payload: " + stringValue(producerRecord.value());
    sampleResult.setSamplerData(result);
  }

  private String stringValue(final Object value) {
    final String result;
    if (value instanceof EnrichedRecord) {
      result = ((EnrichedRecord) value).getGenericRecord().toString();
    } else {
      result = value.toString();
    }
    return result;
  }

  private void fillSampleResult(final SampleResult sampleResult, final String respondeData, final boolean successful) {
    sampleResult.setResponseData(respondeData, StandardCharsets.UTF_8.name());
    sampleResult.setSuccessful(successful);
    sampleResult.sampleEnd();
  }

  private String prettyPrint(final RecordMetadata recordMetadata) {
    return String.format(TEMPLATE, recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
  }

  private Object getObject(final EnrichedRecord messageVal, final String serializer) {
    return (serializer.contains("com.sngular.kloadgen") && !serializer.contains("Generic")) ? messageVal : messageVal.getGenericRecord();
  }
}

