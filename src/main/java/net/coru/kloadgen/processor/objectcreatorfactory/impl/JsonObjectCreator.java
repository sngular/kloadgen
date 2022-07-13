package net.coru.kloadgen.processor.objectcreatorfactory.impl;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.processor.model.SchemaProcessorPOJO;
import net.coru.kloadgen.processor.objectcreatorfactory.ObjectCreator;
import net.coru.kloadgen.processor.util.SchemaProcessorUtils;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;

public class JsonObjectCreator implements ObjectCreator {

  private static final StatelessGeneratorTool STATELESS_GENERATOR_TOOL = new StatelessGeneratorTool();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Map<String, ObjectNode> entity = new HashMap<>();

  @Override
  public Object createMap(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerMap) {
    Object map = JsonNodeFactory.instance.objectNode();
    if (pojo.isLastFilterTypeOfLastElement()) {
      map = createFinalMap(pojo);
    } else if (!pojo.getFieldValuesList().contains("null")) {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        final String key = generateString(pojo.getValueLength());
        Object returnObject;
        if (i == pojo.getFieldSize() - 1) {
          returnObject = generateFunction.apply(pojo.getFieldExpMappingsQueue(), pojo);
        } else {
          try {
            SchemaProcessorPOJO newPojo = (SchemaProcessorPOJO) pojo.clone();
            newPojo.setFieldExpMappingsQueue(pojo.getFieldExpMappingsQueue().clone());
            returnObject = generateFunction.apply(pojo.getFieldExpMappingsQueue().clone(), newPojo);
          } catch (CloneNotSupportedException e) {
            throw new KLoadGenException("Error cloning POJO");
          }
        }
        if (null != returnObject) {
          ((ObjectNode) map).set(key, OBJECT_MAPPER.convertValue(returnObject, JsonNode.class));
        }
      }
    }
    entity.get(pojo.getRootFieldName()).putPOJO(pojo.getFieldNameSubEntity(), map);
    return isInnerMap ? map : entity.get(pojo.getRootFieldName());
  }

  @Override
  public Object createArray(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerArray) {
    ArrayNode nodeArray = OBJECT_MAPPER.createArrayNode();
    if (pojo.isLastFilterTypeOfLastElement()) {
      nodeArray = createFinalArray(pojo);
    } else if (!pojo.getFieldValuesList().contains("null")) {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        Object returnObject;
        if (i == pojo.getFieldSize() - 1) {
          returnObject = generateFunction.apply(pojo.getFieldExpMappingsQueue(), pojo);
        } else {
          try {
            SchemaProcessorPOJO newPojo = (SchemaProcessorPOJO) pojo.clone();
            newPojo.setFieldExpMappingsQueue(pojo.getFieldExpMappingsQueue().clone());
            returnObject = generateFunction.apply(pojo.getFieldExpMappingsQueue().clone(), newPojo);
          } catch (CloneNotSupportedException e) {
            throw new KLoadGenException("Error cloning POJO");
          }
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
  public Object createValueObject(final SchemaProcessorPOJO pojo) {
    ObjectNode object = entity.get(pojo.getRootFieldName());
    object.putPOJO(pojo.getFieldNameSubEntity(), OBJECT_MAPPER.convertValue(
        STATELESS_GENERATOR_TOOL.generateObject(pojo.getFieldNameSubEntity(), pojo.getValueType(), pojo.getValueLength(), pojo.getFieldValuesList()), JsonNode.class));
    return object;
  }

  @Override
  public Object assignRecord(final SchemaProcessorPOJO pojo) {
    ObjectNode parentNode = entity.get(pojo.getRootFieldName());
    parentNode.set(pojo.getFieldNameSubEntity(), entity.get(pojo.getFieldNameSubEntity()));
    return parentNode;
  }

  @Override
  public void createRecord(final String objectName, final String completeFieldName) {
    entity.put(objectName, JsonNodeFactory.instance.objectNode());
  }

  @Override
  public Object generateRecord() {return entity.get("root");}

  @Override
  public Object generateSubEntityRecord(Object objectRecord) {
    return objectRecord;
  }

  @Override
  public boolean isOptionalFieldAccordingToSchema(final String completeFieldName, final String fieldName, final int level) {
    return true;
  }

  private ArrayNode createFinalArray(final SchemaProcessorPOJO pojo) {
    return OBJECT_MAPPER.convertValue(
        STATELESS_GENERATOR_TOOL.generateArray(pojo.getFieldNameSubEntity(), SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()), pojo.getFieldSize(),
                                               pojo.getValueLength(), pojo.getFieldValuesList()), ArrayNode.class);
  }

  private ObjectNode createFinalMap(final SchemaProcessorPOJO pojo) {
    return OBJECT_MAPPER.convertValue(
        STATELESS_GENERATOR_TOOL.generateMap(SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()), pojo.getValueLength(), pojo.getFieldValuesList(),
                                             pojo.getFieldSize()), ObjectNode.class);
  }

  private String generateString(final Integer valueLength) {
    return String.valueOf(STATELESS_GENERATOR_TOOL.generateRandomString(valueLength));
  }

}
