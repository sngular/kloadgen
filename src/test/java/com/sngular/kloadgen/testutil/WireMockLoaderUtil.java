package com.sngular.kloadgen.testutil;

import static com.github.tomakehurst.wiremock.common.Json.read;

import java.io.IOException;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.stubbing.StubMappingCollection;

public final class WireMockLoaderUtil {

    private final FileHelper fileHelper = new FileHelper();

    public void setUpMocks(final WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        StubMappingCollection mappingCollection = deserialize(fileHelper.getContent("/mappings/schema_registry_stub.json"));
        mappingCollection.getMappings().forEach(stubMapping -> wmRuntimeInfo.getWireMock().register(stubMapping));
    }

    private StubMappingCollection deserialize(String fileContent) {
        return read(fileContent, StubMappingCollection.class);
    }

}
