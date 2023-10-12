package com.sngular.kloadgen.sampler;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.ApiExtractor;
import com.sngular.kloadgen.extractor.asyncapi.AsyncApiExtractorImpl;
import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.loadgen.BaseLoadGenerator;
import com.sngular.kloadgen.loadgen.impl.JsonSRLoadGenerator;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.util.ProducerKeysHelper;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GUIMenuSortOrder(Integer.MAX_VALUE)
public class AsyncApiSampler extends AbstractSampler implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncApiSampler.class);

  private static final String TEMPLATE = "Topic: %s, partition: %s, offset: %s";

  private transient AsyncApiFile asyncApiFile;

  private transient ApiExtractor apiExtractor;

  private String asyncApiFileStr;

  private String asyncApiServerName;

  private String asyncApiSchemaName;

  private transient BaseLoadGenerator generator;

  private final transient ObjectMapper mapper = new ObjectMapper();

  public AsyncApiSampler() {
    apiExtractor = new AsyncApiExtractorImpl();
  }

  @Override
  public final boolean applies(final ConfigTestElement configElement) {

    generator = new JsonSRLoadGenerator();
    return super.applies(configElement);
  }

  @Override
  public final SampleResult sample(final Entry entry) {

    final var sampleResult = new SampleResult();
    try (final KafkaProducer<Object, Object> producer = new KafkaProducer<>(extractProps(asyncApiServerName))) {
      final var messageVal = generator.nextMessage();
      if (Objects.nonNull(messageVal)) {
        final var producerRecord = getProducerRecord(messageVal, enrichedKeyFlag(), enrichedValueFlag());
        fillSamplerResult(producerRecord, sampleResult);
        final var result = producer.send(producerRecord, (metadata, e) -> {
          if (e != null) {
            LOG.error("Send failed for record {}", producerRecord, e);
            throw new KLoadGenException("Failed to sent message due ", e);
          }
        });
        fillSampleResult(sampleResult, prettyPrint(result.get()), true);
      } else {
        LOG.error("Failed to Generate message");
        fillSampleResult(sampleResult, "Failed to Generate message", false);
      }
    } catch (final KLoadGenException | InterruptedException | ExecutionException e) {
      LOG.error("Failed to send message", e);
      fillSampleResult(sampleResult, e.getMessage() != null ? e.getMessage() : "", false);
    }
    sampleResult.sampleStart();

    return sampleResult;
  }

  private Map<String, Object> extractProps(String asyncApiServerName) {
    final var properties = new HashMap<String, Object>();
    final var asyncApiBrokerProps = apiExtractor.getBrokerData(asyncApiFile).get(asyncApiServerName).getPropertiesMap();
    SamplerUtil.getCommonProducerDefaultParameters().forEach(property ->
       properties.put(property.getName(), property.getStringValue()));
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, asyncApiBrokerProps.get("url"));
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "GenericJsonRecordSerializer.class");
    properties.put(ProducerKeysHelper.KAFKA_TOPIC_CONFIG, asyncApiSchemaName);
    return properties;
  }

  public final String getAsyncApiFileStr() throws JsonProcessingException {
    return mapper.writeValueAsString(asyncApiFileStr);
  }

  public final void setAsyncApiFileStr(String asyncApiFileStr) {
    this.asyncApiFileStr = asyncApiFileStr;
    try {
      this.asyncApiFile = mapper.createParser(asyncApiFileStr).readValueAs(AsyncApiFile.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public final AsyncApiFile getAsyncApiFile() {
    try {
      asyncApiFile = mapper.createParser(this.getPropertyAsString("asyncapifilestr")).readValueAs(AsyncApiFile.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return asyncApiFile;
  }

  public final void setAsyncApiFile(final AsyncApiFile asyncApiFile) {
    try {
      this.setProperty("asyncapifilestr", mapper.writeValueAsString(asyncApiFile));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    this.asyncApiFile = asyncApiFile;
  }

  public final String getAsyncApiServerName() {
    return asyncApiServerName;
  }

  public final void setAsyncApiServerName(final String asyncApiServerName) {
    this.setProperty("asyncApiServerName", asyncApiServerName);
    this.asyncApiServerName = asyncApiServerName;
  }

  public final String getAsyncApiSchemaName() {
    return asyncApiSchemaName;
  }

  public final void setAsyncApiSchemaName(final String asyncApiSchemaName) {
    this.setProperty("asyncapischemaname", asyncApiSchemaName);
    this.asyncApiSchemaName = asyncApiSchemaName;
  }

  private ProducerRecord<Object, Object> getProducerRecord(final EnrichedRecord messageVal, final boolean keyFlag, final boolean valueFlag) {
    final ProducerRecord<Object, Object> producerRecord;
    producerRecord = new ProducerRecord<>(asyncApiSchemaName, getObject(messageVal, valueFlag));
    return producerRecord;
  }

  private Boolean enrichedKeyFlag() {
    return false;
  }

  private Boolean enrichedValueFlag() {
    return false;
  }

  private void fillSamplerResult(final ProducerRecord<Object, Object> producerRecord, final SampleResult sampleResult) {
    final String result = "key: " + producerRecord.key() + ", payload: " + producerRecord.value();
    sampleResult.setSamplerData(result);
  }

  private void fillSampleResult(final SampleResult sampleResult, final String responseData, final boolean successful) {
    sampleResult.setResponseData(responseData, StandardCharsets.UTF_8.name());
    sampleResult.setSuccessful(successful);
    sampleResult.sampleEnd();
  }

  private String prettyPrint(final RecordMetadata recordMetadata) {
    return String.format(TEMPLATE, recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
  }

  private Object getObject(final EnrichedRecord messageVal, final boolean isKloadSerializer) {
    return isKloadSerializer ? messageVal : messageVal.getGenericRecord();
  }
}
