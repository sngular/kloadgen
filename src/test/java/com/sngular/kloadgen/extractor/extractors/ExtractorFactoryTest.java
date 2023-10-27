package com.sngular.kloadgen.extractor.extractors;

import com.sngular.kloadgen.extractor.extractors.avro.AvroExtractor;
import com.sngular.kloadgen.extractor.extractors.json.JsonExtractor;
import com.sngular.kloadgen.extractor.extractors.protobuff.ProtobuffExtractor;
import com.sngular.kloadgen.model.FieldValueMapping;
import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ExtractorFactoryTest {

    @Mock
    private AvroExtractor avroExtractor;

    @Mock
    private static JsonExtractor jsonExtractor;

    @Mock
    private static ProtobuffExtractor protobuffExtractor;

    @Captor
    private ArgumentCaptor<Object> argumentCaptor;

    private ExtractorFactory extractorFactory;

    @Before
    public void init () {
        ExtractorFactory.configExtractorFactory(avroExtractor, jsonExtractor, protobuffExtractor);
    }

    @Test
    void flatPropertiesList() {
        when(avroExtractor.processSchema(argumentCaptor.capture(), ArgumentMatchers.any())).thenReturn();
        ExtractorFactory.flatPropertiesList()
        assertThat(argumentCaptor.getValue()).isExactlyInstanceOf(Schema.class);
    }
}