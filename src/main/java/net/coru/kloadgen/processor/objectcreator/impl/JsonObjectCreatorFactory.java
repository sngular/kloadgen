package net.coru.kloadgen.processor.objectcreator.impl;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.coru.kloadgen.processor.SchemaProcessorUtils;
import net.coru.kloadgen.processor.objectcreator.ObjectCreator;
import net.coru.kloadgen.processor.objectcreator.model.SchemaProcessorPOJO;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;

public class JsonObjectCreatorFactory implements ObjectCreator {

  private static final StatelessGeneratorTool STATELESS_GENERATOR_TOOL = new StatelessGeneratorTool();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Map<String, ObjectNode> entity = new HashMap<>();

  @Override
  public Object createMap(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerMap) {

    final Object map;
    if (pojo.isLastFilterTypeOfLastElement()) {
      map = createFinalMap(pojo);
    } else {
      map = JsonNodeFactory.instance.objectNode();
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        final String key = generateString(pojo.getValueLength());
        if (i == pojo.getFieldSize() - 1) {
          ((ObjectNode) map).set(key, OBJECT_MAPPER.convertValue(generateFunction.apply(pojo.getFieldExpMappingsQueue(), pojo), JsonNode.class));
        } else {
          ((ObjectNode) map).set(key, OBJECT_MAPPER.convertValue(generateFunction.apply(pojo.getFieldExpMappingsQueue().clone(), pojo), JsonNode.class));
        }
      }
    }

    //entity.get(pojo.getRootFieldName()).putPOJO(pojo.getFieldNameSubEntity(), map);
    return map;
  }

  @Override
  public Object createArray(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerArray) {

    ArrayNode nodeArray = OBJECT_MAPPER.createArrayNode();

    if (pojo.isLastFilterTypeOfLastElement()) {
      nodeArray = createFinalArray(pojo);
    } else {
      for (int i = 0; i < pojo.getFieldSize(); i++) {
        final JsonNode object;
        if (i == pojo.getFieldSize() - 1) {
          object = (JsonNode) generateFunction.apply(pojo.getFieldExpMappingsQueue(), pojo);
        } else {
          //nodeArray.add((ObjectNode) generateFunction.apply(pojo.getFieldExpMappingsQueue().clone(), pojo));
          object = (JsonNode) generateFunction.apply(pojo.getFieldExpMappingsQueue().clone(), pojo);
        }
        if (object.isObject()) {
          nodeArray.add(OBJECT_MAPPER.convertValue(object, ObjectNode.class));
        } else if (object.isArray()) {
          nodeArray.add(OBJECT_MAPPER.convertValue(object, ArrayNode.class));
        }
      }
    }

    //entity.get(pojo.getRootFieldName()).putArray(pojo.getFieldNameSubEntity()).addAll(nodeArray);
    return nodeArray;
  }

  @Override
  public Object createValueObject(final SchemaProcessorPOJO pojo) {

    final ObjectNode object = entity.get(pojo.getRootFieldName());
    object.putPOJO(pojo.getCompleteFieldName(), OBJECT_MAPPER.convertValue(
        STATELESS_GENERATOR_TOOL.generateObject(pojo.getFieldNameSubEntity(), pojo.getValueType(), pojo.getValueLength(), pojo.getFieldValuesList()), JsonNode.class));
    return object;
  }

  @Override
  public Object assignRecord(final SchemaProcessorPOJO pojo) {

    final ObjectNode parentNode = entity.get(pojo.getRootFieldName());
    parentNode.set(pojo.getFieldNameSubEntity(), entity.get(pojo.getFieldNameSubEntity()));
    return parentNode;
  }

  @Override
  public void createRecord(final String objectName, final String completeFieldName) {
    entity.put(objectName, JsonNodeFactory.instance.objectNode());
  }

  @Override
  public Object generateRecord() {
    return entity.get("root");
  }

  @Override
  public Object generateSubEntityRecord(final Object objectRecord) {
    return objectRecord;
  }

  @Override
  public boolean isOptional(final SchemaProcessorPOJO pojo) {
    return true;
  }

  private Object createFinalMap(final SchemaProcessorPOJO pojo) {
    return STATELESS_GENERATOR_TOOL.generateMap(SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()), pojo.getValueLength(), pojo.getFieldValuesList(),
                                                pojo.getFieldSize());
  }

  private String generateString(final Integer valueLength) {
    return String.valueOf(STATELESS_GENERATOR_TOOL.generateRandomString(valueLength));
  }

  private ArrayNode createFinalArray(final SchemaProcessorPOJO pojo) {
    return OBJECT_MAPPER.convertValue(
        STATELESS_GENERATOR_TOOL.generateArray(pojo.getFieldNameSubEntity(), SchemaProcessorUtils.getOneDimensionValueType(pojo.getValueType()), pojo.getFieldSize(),
                                               pojo.getValueLength(), pojo.getFieldValuesList()), ArrayNode.class);
  }

}
