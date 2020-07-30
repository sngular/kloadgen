package net.coru.kloadgen.loadgen;

import java.util.List;
import java.util.Map;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;

public interface BaseLoadGenerator {

    void setUpGenerator(Map<String, String> originals, String avroSchemaName, List<FieldValueMapping> fieldExprMappings);

    void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings);

    EnrichedRecord nextMessage();
}
