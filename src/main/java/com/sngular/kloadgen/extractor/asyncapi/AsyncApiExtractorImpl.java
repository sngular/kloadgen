package com.sngular.kloadgen.extractor.asyncapi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sngular.kloadgen.common.tools.ApiTool;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.ApiExtractor;
import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.extractor.model.AsyncApiSR;
import com.sngular.kloadgen.extractor.model.AsyncApiSchema;
import com.sngular.kloadgen.extractor.model.AsyncApiServer;

public class AsyncApiExtractorImpl implements ApiExtractor {

  public static final String ERROR_WRONG_ASYNC_API_SCHEMA = "Wrong AsyncApi Schema";

  public static final String SERVERS = "servers";

  public static final String CHANNELS = "channels";

  final ObjectMapper om = new ObjectMapper(new YAMLFactory());

  @Override
  public AsyncApiFile processFile(final File apiFile) {
    var builder = AsyncApiFile.builder();
    try {
      final JsonNode openApi = om.readTree(apiFile);
      builder.apiServerList(nodeToApServer(ApiTool.getNode(openApi, SERVERS)));
      builder.apiSchemaList(nodeToSchema(ApiTool.getNode(openApi, CHANNELS)));
    } catch (IOException e) {
      throw new KLoadGenException(ERROR_WRONG_ASYNC_API_SCHEMA, e);
    }

    return builder.build();
  }

  private List<AsyncApiSchema> nodeToSchema(final JsonNode node) {
    var schemaList = new ArrayList<AsyncApiSchema>();
    node.fields().forEachRemaining(channel -> schemaList.add(mapNodeToSchema(channel.getKey(), channel.getValue())));
    return schemaList;
  }

  private AsyncApiSchema mapNodeToSchema(final String topic, final JsonNode node) {
    var builder = AsyncApiSchema.builder();
    builder.topicName(topic);
    var message = ApiTool.getNode(node, "message");
    if (ApiTool.hasNode(message, "bindings")) {
      builder.key(true);
     // builder.keyType(ApiTool.findValue(message, "key"));
    }
    return builder.build();
  }

  private List<AsyncApiServer> nodeToApServer(final JsonNode node) {
    var serverList = new ArrayList<AsyncApiServer>();
    node.fields().forEachRemaining(server -> serverList.add(mapNodeToServer(server.getKey(), server.getValue())));
    return serverList;
  }

  private AsyncApiServer mapNodeToServer(final String serverName, final JsonNode server) {
    var builder = AsyncApiServer.builder();
    builder.name(serverName);
    builder.url(ApiTool.getNodeAsString(server, "url"));
    builder.protocol(ApiTool.getNodeAsString(server, "protocol"));
    builder.description(ApiTool.getNodeAsString(server, "description"));
    return builder.build();
  }

  @Override
  public List<AsyncApiServer> getBrokerData(final AsyncApiFile asyncApiFile) {
    return asyncApiFile.getApiServerList();
  }

  @Override
  public List<AsyncApiSR> getSchemaRegistryData(final AsyncApiFile asyncApiFile) {
    return asyncApiFile.getApiAsyncApiSRList();
  }

  @Override
  public AsyncApiSchema getSchemaData(final AsyncApiFile asyncApiFile) {
    return asyncApiFile.getApiSchemaList().get(0);
  }
}
