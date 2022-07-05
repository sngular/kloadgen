package net.coru.kloadgen.processor.objectcreator.model;

import net.coru.kloadgen.model.ConstraintTypeEnum;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;

public class GenerationFunctionPOJO {

  private String rootFieldName;

  private ArrayDeque<?> fieldExpMappingsQueue;

  private String objectName;

  private String fieldNameSubEntity;

  private String completeFieldName;

  private String completeTypeFilterChain;

  private Integer valueLength;

  private String valueType;

  private List<String> fieldValuesList;

  private Map<ConstraintTypeEnum, String> constraints;

  private int level;

  public GenerationFunctionPOJO(
      String rootFieldName, ArrayDeque<?> fieldExpMappingsQueue, String objectName, String fieldNameSubEntity, String completeFieldName,
      String completeTypeFilterChain, String valueType,
      Integer valueLength, List<String> fieldValuesList, Map<ConstraintTypeEnum, String> constraints, int level) {
    this.rootFieldName = rootFieldName;
    this.fieldExpMappingsQueue = fieldExpMappingsQueue;
    this.objectName = objectName;
    this.fieldNameSubEntity = fieldNameSubEntity;
    this.completeFieldName = completeFieldName;
    this.completeTypeFilterChain = completeTypeFilterChain;
    this.valueType = valueType;
    this.valueLength = valueLength;
    this.fieldValuesList = fieldValuesList;
    this.constraints = constraints;
    this.level = level;
  }

  public String getRootFieldName() {
    return rootFieldName;
  }

  public ArrayDeque<?> getFieldExpMappingsQueue() {
    return fieldExpMappingsQueue;
  }

  public String getObjectName() {
    return objectName;
  }

  public String getFieldNameSubEntity() {
    return fieldNameSubEntity;
  }

  public String getCompleteTypeFilterChain() {
    return completeTypeFilterChain;
  }

  public Integer getValueLength() {
    return valueLength;
  }

  public List<String> getFieldValuesList() {
    return fieldValuesList;
  }

  public String getCompleteFieldName() {
    return completeFieldName;
  }

  public String getValueType() {
    return valueType;
  }

  public Map<ConstraintTypeEnum, String> getConstraints() {
    return constraints;
  }

  public void setConstraints(Map<ConstraintTypeEnum, String> constraints) {
    this.constraints = constraints;
  }

  public int getLevel() {
    return level;
  }
}
