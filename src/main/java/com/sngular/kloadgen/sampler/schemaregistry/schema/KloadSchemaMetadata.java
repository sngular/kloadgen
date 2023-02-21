package com.sngular.kloadgen.sampler.schemaregistry.schema;

import lombok.Getter;

@Getter
public class KloadSchemaMetadata {

    private String id;
    private String version;
    private String schemaType;

    public KloadSchemaMetadata(String id, String version, String schemaType) {
        this.id = id;
        this.version = version;
        this.schemaType = schemaType;
    }
}
