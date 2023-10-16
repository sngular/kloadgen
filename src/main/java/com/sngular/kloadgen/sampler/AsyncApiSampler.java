package com.sngular.kloadgen.sampler;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.ApiExtractor;
import com.sngular.kloadgen.extractor.asyncapi.AsyncApiExtractorImpl;
import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.loadgen.BaseLoadGenerator;
import com.sngular.kloadgen.loadgen.impl.JsonSRLoadGenerator;
import com.sngular.kloadgen.model.FieldValueMapping;
import com.sngular.kloadgen.model.PropertyMapping;
import com.sngular.kloadgen.serializer.EnrichedRecord;
import com.sngular.kloadgen.serializer.GenericJsonRecordSerializer;
import com.sngular.kloadgen.util.PropsKeysHelper;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
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

  private final transient ApiExtractor apiExtractor;

  private JsonNode asyncApiFileNode;

  private final transient BaseLoadGenerator generator;

  private final transient ObjectMapper mapper = new ObjectMapper();

  public AsyncApiSampler() {
    apiExtractor = new AsyncApiExtractorImpl();
    generator = new JsonSRLoadGenerator();
  }

  @Override
  public final boolean applies(final ConfigTestElement configElement) {
    return super.applies(configElement);
  }

  @Override
  public final SampleResult sample(final Entry entry) {

    final var sampleResult = new SampleResult();
    sampleResult.setThreadName("AsyncApi Sampler");
    sampleResult.sampleStart();
    try (final KafkaProducer<Object, Object> producer = new KafkaProducer<>(extractProps())) {
      generator.setUpGenerator(getSchemaFieldConfiguration());
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

    return sampleResult;
  }

  private Map<String, Object> extractProps() {
    final var properties = new HashMap<String, Object>();
    getBrokerConfiguration().forEach(property -> properties.put(property.getPropertyName(), property.getPropertyValue()));
    properties.put(PropsKeysHelper.SCHEMA_KEYED_MESSAGE_KEY, Boolean.FALSE);
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GenericJsonRecordSerializer.class.getCanonicalName());
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, GenericJsonRecordSerializer.class.getCanonicalName());
    return properties;
  }

  public final AsyncApiFile getAsyncApiFileNode() {
    AsyncApiFile asyncApiFile = null;
    if (Objects.isNull(asyncApiFileNode)) {
      try {
        asyncApiFileNode = mapper.readTree(this.getPropertyAsString("asyncapifilestr"));
      } catch (final IOException e) {
        throw new KLoadGenException(e);
      }
    }
    if (Objects.nonNull(asyncApiFileNode) && !asyncApiFileNode.isMissingNode()) {
      asyncApiFile = apiExtractor.processNode(asyncApiFileNode);
    }
    return asyncApiFile;
  }

  public final void setAsyncApiFileNode(final JsonNode asyncApiFileNode) {
    this.asyncApiFileNode = asyncApiFileNode;
    try {
      this.setProperty("asyncapifilestr", mapper.writeValueAsString(asyncApiFileNode));
    } catch (final JsonProcessingException e) {
      throw new KLoadGenException(e);
    }
  }

  public final void setAsyncApiServerName(final String asyncApiServerName) {
    this.setProperty("asyncApiServerName", asyncApiServerName);
  }

  public final void setSchemaFieldConfiguration(final List<FieldValueMapping> asyncApiServerName) {
    this.setProperty(new CollectionProperty("schemaFieldConfiguration", asyncApiServerName));
  }

  public final List<FieldValueMapping> getSchemaFieldConfiguration() {
    final List<FieldValueMapping> propertyList = new ArrayList<>();
    for (TestElementProperty elementProperty : (List<TestElementProperty>) this.getProperty("schemaFieldConfiguration").getObjectValue()) {
      propertyList.add(FieldValueMapping
                           .builder()
                           .fieldName(elementProperty.getElement().getPropertyAsString("fieldName"))
                           .fieldType(elementProperty.getElement().getPropertyAsString("fieldType"))
                           .required(elementProperty.getElement().getPropertyAsBoolean("required", false))
                           .valueLength(elementProperty.getElement().getPropertyAsInt("valueLength", 0))
                           .fieldValueList(elementProperty.getElement().getPropertyAsString("fieldValueList"))
                           .build());
    }
    return propertyList;
  }

  public final void setBrokerConfiguration(final List<PropertyMapping> asyncApiServerName) {
    this.setProperty(new CollectionProperty("brokerConfiguration", asyncApiServerName));
  }

  public final List<PropertyMapping> getBrokerConfiguration() {
    final List<PropertyMapping> propertyList = new ArrayList<>();
    for (TestElementProperty elementProperty : (List<TestElementProperty>) this.getProperty("brokerConfiguration").getObjectValue()) {
      propertyList.add(PropertyMapping
          .builder()
          .propertyName(elementProperty.getElement().getPropertyAsString("propertyName"))
          .propertyValue(elementProperty.getElement().getPropertyAsString("propertyValue"))
          .build());
    }
    return propertyList;
  }

  public final String getAsyncApiSchemaName() {
    return this.getPropertyAsString("asyncapischemaname");
  }

  public final void setAsyncApiSchemaName(final String asyncApiSchemaName) {
    this.setProperty("asyncapischemaname", asyncApiSchemaName);
  }

  private ProducerRecord<Object, Object> getProducerRecord(final EnrichedRecord messageVal, final boolean keyFlag, final boolean valueFlag) {
    final ProducerRecord<Object, Object> producerRecord;
    producerRecord = new ProducerRecord<>(getAsyncApiSchemaName(), getObject(messageVal, valueFlag));
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
