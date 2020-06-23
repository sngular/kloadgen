package net.coru.kloadgen.loadgen;

import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.serializer.EnrichedRecord;

public interface BaseLoadGenerator {

    void setUpGeneratorFromRegistry(String avroSchemaName, List<FieldValueMapping> fieldExprMappings);

    void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings);

    EnrichedRecord nextMessage();
}
