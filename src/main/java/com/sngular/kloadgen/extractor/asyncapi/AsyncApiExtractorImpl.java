package com.sngular.kloadgen.extractor.asyncapi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sngular.kloadgen.common.tools.ApiTool;
import com.sngular.kloadgen.common.tools.MapperUtil;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.extractor.ApiExtractor;
import com.sngular.kloadgen.extractor.model.AsyncApiFile;
import com.sngular.kloadgen.extractor.model.AsyncApiSR;
import com.sngular.kloadgen.extractor.model.AsyncApiSchema;
import com.sngular.kloadgen.extractor.model.AsyncApiServer;
import com.sngular.kloadgen.model.FieldValueMapping;
import org.apache.commons.lang3.StringUtils;

public class AsyncApiExtractorImpl implements ApiExtractor {

  private static final Set<String> COMPLEX_TYPES = Set.of("record", "array");
  private static final String ERROR_WRONG_ASYNC_API_SCHEMA = "Wrong AsyncApi Schema";

  private static final String SERVERS = "servers";

  private static final String CHANNELS = "channels";

  private final ObjectMapper om = new ObjectMapper(new YAMLFactory());

  @Override
  public final AsyncApiFile processFile(final File apiFile) {
    final var builder = AsyncApiFile.builder();
    try {
      final JsonNode openApi = om.readTree(apiFile);
      builder.apiServerList(nodeToApServer(ApiTool.getNode(openApi, SERVERS)));
      builder.apiSchemaList(nodeToSchema(openApi));
    } catch (final IOException e) {
      throw new KLoadGenException(ERROR_WRONG_ASYNC_API_SCHEMA, e);
    }

    return builder.build();
  }

  private Map<String, AsyncApiSchema> nodeToSchema(final JsonNode openApi) {
    final var channels = ApiTool.getNode(openApi, CHANNELS);
    final var schemaList = new HashMap<String, AsyncApiSchema>();
    final Map<String, JsonNode> totalSchemas = new HashMap<>(ApiTool.getComponentSchemas(openApi));
    totalSchemas.putAll(ApiTool.getComponentMessages(openApi));
    channels
        .fields()
        .forEachRemaining(channel ->
                          schemaList.put(channel.getKey(),
                                         mapNodeToSchema(totalSchemas, channel.getKey(), channel.getValue())));
    return schemaList;
  }

  private AsyncApiSchema mapNodeToSchema(final Map<String, JsonNode> components, final String topic, final JsonNode node) {
    final var antiLoopList = new ArrayList<String>();
    final var builder = AsyncApiSchema.builder();
    builder.topicName(topic);
    final var message = ApiTool.findNodeValue(node, "message");
    if (ApiTool.hasNode(message, "bindings")) {
      builder.key(true);
     // builder.keyType(ApiTool.findValue(message, "key"));
    }
    builder.model(messageToFieldList(message, components, antiLoopList));
    return builder.build();
  }

  private List<FieldValueMapping> messageToFieldList(final JsonNode message, final Map<String, JsonNode> components, final List<String> antiLoopList) {
    var payload = ApiTool.getNode(message, "payload");
    if (ApiTool.hasRef(message)) {
      payload = solveRef(message, components, antiLoopList, "messages");
    } else if (!ApiTool.hasNode(message, "payload")) {
      throw new KLoadGenException("AsyncApi format still not supported");
    }


    return extractFieldList(payload, components, antiLoopList);
  }

  private List<FieldValueMapping> extractFieldList(final JsonNode payload, final Map<String, JsonNode> components, final List<String> antiLoopList) {
    var finalPayload = payload;
    if (ApiTool.hasRef(payload)) {
      final var payloadPath = MapperUtil.splitName(ApiTool.getRefValue(payload));
      final String objectName = payloadPath[payloadPath.length - 1];
      if (!antiLoopList.contains(objectName)) {
        finalPayload = components.get("schemas/" + objectName);
        antiLoopList.add(objectName);
      }
    }
    if (!ApiTool.hasProperties(finalPayload)) {
      throw new KLoadGenException("Wrong Payload for message ");
    }
    return new ArrayList<>(processPayload(finalPayload, "", components, antiLoopList));
  }

  private List<FieldValueMapping> processPayload(final JsonNode finalPayload, final String root, final Map<String, JsonNode> components, final List<String> antiLoopList) {
    final var fieldList = new ArrayList<FieldValueMapping>();
    if (ApiTool.hasRef(finalPayload)) {
      fieldList.addAll(processPayload(solveRef(finalPayload, components, antiLoopList, "schemas"), root, components, antiLoopList));
    } else if (ApiTool.hasAdditionalProperties(finalPayload)) {
      fieldList.addAll(processMap(finalPayload, root, components, antiLoopList));
    } else {
      for (Iterator<Entry<String, JsonNode>> it = ApiTool.getProperties(finalPayload); it.hasNext();) {
        final var property = it.next();
        final var propertyDef = property.getValue();
        if (ApiTool.hasRef(propertyDef)) {
          final var solvedPayload = solveRef(propertyDef, components, antiLoopList, "schemas");
          if (Objects.nonNull(solvedPayload)) {
            fieldList.addAll(processPayload(solvedPayload, calculateName(property.getKey(), root), components, antiLoopList));
          }
        } else if (COMPLEX_TYPES.contains(ApiTool.getType(propertyDef)) || ApiTool.hasAdditionalProperties(propertyDef)) {
          fieldList.addAll(calculatePayload(propertyDef, calculateName(property.getKey(), root), components, antiLoopList));
        } else {
          fieldList.add(FieldValueMapping
                          .builder()
                          .fieldName(calculateName(property.getKey(), root))
                          .fieldType(ApiTool.getType(propertyDef))
                          .build());
        }
      }
    }
    return fieldList;
  }

  private List<FieldValueMapping> calculatePayload(final JsonNode propertyDef, final String root, final Map<String, JsonNode> components, final List<String> antiLoopList) {
    final var fieldList = new ArrayList<FieldValueMapping>();
    if (!ApiTool.hasAdditionalProperties(propertyDef)) {
      switch (ApiTool.getType(propertyDef)) {
        case "object" -> fieldList.addAll(processPayload(propertyDef, root, components, antiLoopList));
        case "array" -> fieldList.addAll(processArray(propertyDef, root, components, antiLoopList));
        default -> {
        }
      }
    } else {
      fieldList.addAll(processMap(propertyDef, root, components, antiLoopList));
    }
    return fieldList;
  }

  private List<FieldValueMapping> processArray(final JsonNode propertyDef, final String root, final Map<String, JsonNode> components, final List<String> antiLoopList) {
    final List<FieldValueMapping> fieldList = new ArrayList<>();

    if (ApiTool.hasItems(propertyDef)) {
      final var itemNode = ApiTool.getItems(propertyDef);
      if (ApiTool.hasRef(itemNode)) {
        fieldList.addAll(processPayload(itemNode, root + "[]", components, antiLoopList));
      } else {
        switch (ApiTool.getType(itemNode)) {
          case "object" -> fieldList.addAll(processPayload(itemNode, root + "[]", components, antiLoopList));
          case "string", "number", "integer", "boolean" -> fieldList.add(FieldValueMapping
                                                                           .builder()
                                                                           .fieldName(root + "[]")
                                                                           .fieldType(ApiTool.getType(itemNode) + "-array")
                                                                           .build());

          default -> throw new KLoadGenException("Unexpected value: " + ApiTool.getType(itemNode));
        }
      }
    } else {
      throw new KLoadGenException(String.format("Wrong Array Definition for %s", root));
    }
    return fieldList;
  }

  private List<FieldValueMapping> processMap(final JsonNode propertyDef, final String root, final Map<String, JsonNode> components, final List<String> antiLoopList) {
    final List<FieldValueMapping> fieldList = new ArrayList<>();

    if (ApiTool.hasAdditionalProperties(propertyDef)) {
      final var itemNode = ApiTool.getAdditionalProperties(propertyDef);

      switch (ApiTool.getType(itemNode)) {
        case "object" -> fieldList.addAll(processPayload(itemNode, root + "[:]", components, antiLoopList));
        case "string", "number", "integer", "boolean" -> fieldList.add(FieldValueMapping
                                                                         .builder()
                                                                         .fieldName(root + "[:]")
                                                                         .fieldType(ApiTool.getType(itemNode) + "-map")
                                                                         .build());
        default -> throw new KLoadGenException("Unexpected value: " + ApiTool.getType(itemNode));
      }
    } else {
      throw new KLoadGenException(String.format("Wrong Array Definition for %s", root));
    }
    return fieldList;
  }

  private String calculateName(final String propertyName, final String root) {
    final String finalName;
    if (StringUtils.isEmpty(root)) {
      finalName = propertyName;
    } else {
      finalName = root + "." + propertyName;
    }
    return finalName;
  }

  private List<AsyncApiServer> nodeToApServer(final JsonNode node) {
    final var serverList = new ArrayList<AsyncApiServer>();
    node.fields().forEachRemaining(server -> serverList.add(mapNodeToServer(server.getKey(), server.getValue())));
    return serverList;
  }

  private JsonNode solveRef(final JsonNode message, final Map<String, JsonNode> components, final List<String> antiLoopList, final String schemaType) {
    JsonNode payload = null;
    final var payloadPath = MapperUtil.splitName(ApiTool.getRefValue(message));
    final var objectName = payloadPath[payloadPath.length - 1];
    if (!antiLoopList.contains(objectName)) {
      payload = components.get(schemaType + "/" + objectName);
      antiLoopList.add(objectName);
    }
    return payload;
  }

  private AsyncApiServer mapNodeToServer(final String serverName, final JsonNode server) {
    final var builder = AsyncApiServer.builder();
    builder.name(serverName);
    builder.url(ApiTool.getNodeAsString(server, "url"));
    builder.protocol(ApiTool.getNodeAsString(server, "protocol"));
    builder.description(ApiTool.getNodeAsString(server, "description"));
    return builder.build();
  }

  @Override
  public final List<AsyncApiServer> getBrokerData(final AsyncApiFile asyncApiFile) {
    return asyncApiFile.getApiServerList();
  }

  @Override
  public final List<AsyncApiSR> getSchemaRegistryData(final AsyncApiFile asyncApiFile) {
    return asyncApiFile.getApiAsyncApiSRList();
  }

  @Override
  public final AsyncApiSchema getSchemaData(final AsyncApiFile asyncApiFile, final String topic) {
    return asyncApiFile.getApiSchemaList().get(topic);
  }

  @Override
  public final Map<String, AsyncApiSchema> getSchemaDataMap(final AsyncApiFile asyncApiFile) {
    return asyncApiFile.getApiSchemaList();
  }
}
