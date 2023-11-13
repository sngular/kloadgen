package com.sngular.kloadgen.processor.objectcreatorfactory.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sngular.kloadgen.exception.KLoadGenException;
import com.sngular.kloadgen.processor.model.SchemaProcessorPOJO;
import com.sngular.kloadgen.processor.objectcreatorfactory.ObjectCreatorFactory;
import com.sngular.kloadgen.processor.util.SchemaProcessorUtils;
import com.sngular.kloadgen.randomtool.generator.StatelessGeneratorTool;

public class JsonObjectCreatorFactory implements ObjectCreatorFactory {

  private static final StatelessGeneratorTool STATELESS_GENERATOR_TOOL = new StatelessGeneratorTool();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Map<String, ObjectNode> entity = new HashMap<>();

  public JsonObjectCreatorFactory() {
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  @Override
  public final Object createMap(final SchemaProcessorPOJO pojo, final Function<SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerMap) {
    ObjectNode map = JsonNodeFactory.instance.objectNode();
    if (pojo.isLastFilterTypeOfLastElement()) {
      map = createFinalMap(pojo);
    } else if (SchemaProcessorUtils.isFieldValueListNotAcceptingNullValues(pojo.getFieldValuesList())) {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        final String key = generateString(pojo.getValueLength());
        final Object returnObject;
        try {
          returnObject = generateFunction.apply(i == pojo.getFieldSize() - 1 ? pojo : (SchemaProcessorPOJO) pojo.clone());
        } catch (final CloneNotSupportedException e) {
          throw new KLoadGenException("Error cloning POJO");
        }
        if (null != returnObject) {
          map.set(key, OBJECT_MAPPER.convertValue(returnObject, JsonNode.class));
        }
      }
    }
    entity.get(pojo.getRootFieldName()).putPOJO(pojo.getFieldNameSubEntity(), map);
    return isInnerMap ? map : entity.get(pojo.getRootFieldName());
  }

  private ObjectNode createFinalMap(final SchemaProcessorPOJO pojo) {
    return OBJECT_MAPPER.convertValue(
        STATELESS_GENERATOR_TOOL.generateMap(SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()), pojo.getValueLength(), pojo.getFieldValuesList(),
                                             pojo.getFieldSize()), ObjectNode.class);
  }

  private String generateString(final Integer valueLength) {
    return String.valueOf(STATELESS_GENERATOR_TOOL.generateRandomString(valueLength));
  }

  @Override
  public final Object createArray(final SchemaProcessorPOJO pojo, final Function<SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerArray) {
    ArrayNode nodeArray = OBJECT_MAPPER.createArrayNode();
    if (pojo.isLastFilterTypeOfLastElement()) {
      nodeArray = createFinalArray(pojo);
    } else if (SchemaProcessorUtils.isFieldValueListNotAcceptingNullValues(pojo.getFieldValuesList())) {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        final Object returnObject;
        try {
          returnObject = generateFunction.apply(i == pojo.getFieldSize() - 1 ? pojo : (SchemaProcessorPOJO) pojo.clone());
        } catch (final CloneNotSupportedException e) {
          throw new KLoadGenException("Error cloning POJO");
        }
        if (null != returnObject) {
          if (((JsonNode) returnObject).isObject()) {
            nodeArray.add(OBJECT_MAPPER.convertValue(returnObject, ObjectNode.class));
          } else if (((JsonNode) returnObject).isArray()) {
            nodeArray.add(OBJECT_MAPPER.convertValue(returnObject, ArrayNode.class));
          }
        }
      }
    }

    entity.get(pojo.getRootFieldName()).putArray(pojo.getFieldNameSubEntity()).addAll(nodeArray);
    return isInnerArray ? nodeArray : entity.get(pojo.getRootFieldName());
  }

  @Override
  public final Object createValueObject(final SchemaProcessorPOJO pojo) {
    final ObjectNode object = entity.get(pojo.getRootFieldName());
    object.putPOJO(pojo.getFieldNameSubEntity(), OBJECT_MAPPER.convertValue(
        STATELESS_GENERATOR_TOOL.generateObject(pojo.getFieldNameSubEntity(), pojo.getValueType(), pojo.getValueLength(), pojo.getFieldValuesList()), JsonNode.class));
    return object;
  }

  @Override
  public final void assignRecord(final SchemaProcessorPOJO pojo) {
    final ObjectNode parentNode = entity.get(pojo.getRootFieldName());
    parentNode.set(pojo.getFieldNameSubEntity(), entity.get(pojo.getFieldNameSubEntity()));
  }

  @Override
  public final void createRecord(final String objectName, final String completeFieldName) {
    entity.put(objectName, JsonNodeFactory.instance.objectNode());
  }

  @Override
  public final Object generateRecord() {
    return entity.get("root");
  }

  @Override
  public final Object generateSubEntityRecord(final Object objectRecord) {
    return objectRecord;
  }

  @Override
  public final boolean isOptionalFieldAccordingToSchema(final String completeFieldName, final String fieldName, final int level) {
    return true;
  }

  @Override
  public final Object getRootNode(final String rootNode) {
    return entity.get(rootNode);
  }

  private ArrayNode createFinalArray(final SchemaProcessorPOJO pojo) {
    return OBJECT_MAPPER.convertValue(
        STATELESS_GENERATOR_TOOL.generateArray(pojo.getFieldNameSubEntity(), SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()), pojo.getFieldSize(),
                                               pojo.getValueLength(), pojo.getFieldValuesList()), ArrayNode.class);
  }

}
