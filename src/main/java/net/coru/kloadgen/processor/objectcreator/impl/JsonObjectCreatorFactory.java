package net.coru.kloadgen.processor.objectcreator.impl;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    return null;
  }

  @Override
  public Object createArray(
      final SchemaProcessorPOJO pojo, final BiFunction<ArrayDeque<?>, SchemaProcessorPOJO, Object> generateFunction, final boolean isInnerArray) {
    return null;
  }

  @Override
  public Object createValueObject(final SchemaProcessorPOJO pojo) {

    ObjectNode object = entity.get(pojo.getRootFieldName());
    object.putPOJO(pojo.getCompleteFieldName(), OBJECT_MAPPER.convertValue(
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
  public boolean isOptional(final SchemaProcessorPOJO pojo) {
    return true;
  }

  public Object assignObject(final String targetObjectName, final String fieldName, final Object objectToAssign) {
    return null;
  }
}
