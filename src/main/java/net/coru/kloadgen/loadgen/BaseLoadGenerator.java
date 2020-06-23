package net.coru.kloadgen.loadgen;

import java.util.List;
import net.coru.kloadgen.model.FieldValueMapping;

public interface BaseLoadGenerator {

    void setUpGeneratorFromRegistry(String avroSchemaName, List<FieldValueMapping> fieldExprMappings);

    void setUpGenerator(String schema, List<FieldValueMapping> fieldExprMappings);

    Object nextMessage();
}
