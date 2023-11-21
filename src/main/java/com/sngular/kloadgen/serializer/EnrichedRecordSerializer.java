package com.sngular.kloadgen.serializer;

import org.apache.kafka.common.serialization.Serializer;

public interface EnrichedRecordSerializer<T extends EnrichedRecord> extends Serializer<T> {

}