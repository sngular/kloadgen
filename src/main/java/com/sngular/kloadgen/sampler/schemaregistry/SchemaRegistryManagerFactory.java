package com.sngular.kloadgen.sampler.schemaregistry;

import lombok.extern.slf4j.Slf4j;
import com.sngular.kloadgen.common.SchemaRegistryEnum;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.sampler.schemaregistry.impl.ApicurioSchemaRegistry;
import com.sngular.kloadgen.sampler.schemaregistry.impl.ConfluentSchemaRegistry;

import java.util.HashMap;
import java.util.Map;

import static com.sngular.kloadgen.common.SchemaRegistryEnum.APICURIO;
import static com.sngular.kloadgen.common.SchemaRegistryEnum.CONFLUENT;

@Slf4j
public class SchemaRegistryManagerFactory {

    static Map<SchemaRegistryEnum, SchemaRegistryManager> schemaRegistryMap = new HashMap<>();

    static {
        schemaRegistryMap.put(CONFLUENT, new ConfluentSchemaRegistry());
        schemaRegistryMap.put(APICURIO, new ApicurioSchemaRegistry());
    }

    public static SchemaRegistryManager getSchemaRegistry(String registry) {
        try {
            SchemaRegistryEnum schemaRegistryEnum = SchemaRegistryEnum.valueOf(registry.toUpperCase());
            return schemaRegistryMap.get(schemaRegistryEnum);
        } catch (IllegalArgumentException e) {
            final String logMsg = "Can not parse the registry " + registry;
            log.error(logMsg, e);
            throw new KLoadGenException("Error obtaining the schema registry manager. " + logMsg);
        }
    }
}
