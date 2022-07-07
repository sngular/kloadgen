package net.coru.kloadgen.processor.objectcreator.model;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;

import net.coru.kloadgen.model.ConstraintTypeEnum;

public class SchemaProcessorPOJO {

  private String rootFieldName;

  private ArrayDeque<?> fieldExpMappingsQueue;

  private String fieldNameSubEntity;

  private String completeFieldName;

  private String completeTypeFilterChain;

  private Integer valueLength;

  private String valueType;

  private List<String> fieldValuesList;

  private boolean lastFilterTypeOfLastElement;

  private Map<ConstraintTypeEnum, String> constraints;

  private int level;

  private int fieldSize;

  public SchemaProcessorPOJO(
      String rootFieldName, ArrayDeque<?> fieldExpMappingsQueue, String fieldNameSubEntity, String completeFieldName,
      String completeTypeFilterChain, int fieldSize, String valueType,
      Integer valueLength, List<String> fieldValuesList, Map<ConstraintTypeEnum, String> constraints, int level, boolean isLastElementType) {
    this.rootFieldName = rootFieldName;
    this.fieldExpMappingsQueue = fieldExpMappingsQueue;
    this.fieldNameSubEntity = fieldNameSubEntity;
    this.completeFieldName = completeFieldName;
    this.completeTypeFilterChain = completeTypeFilterChain;
    this.fieldSize = fieldSize;
    this.valueType = valueType;
    this.valueLength = valueLength;
    this.fieldValuesList = fieldValuesList;
    this.constraints = constraints;
    this.level = level;
    this.lastFilterTypeOfLastElement = isLastElementType;
  }

  public String getRootFieldName() {
    return rootFieldName;
  }

  public void setRootFieldName(final String rootFieldName) {
    this.rootFieldName = rootFieldName;
  }

  public ArrayDeque<?> getFieldExpMappingsQueue() {
    return fieldExpMappingsQueue;
  }

  public String getFieldNameSubEntity() {
    return fieldNameSubEntity;
  }

  public void setFieldNameSubEntity(final String fieldNameSubEntity) {
    this.fieldNameSubEntity = fieldNameSubEntity;
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

  public int getLevel() {
    return level;
  }

  public boolean isLastFilterTypeOfLastElement() {
    return lastFilterTypeOfLastElement;
  }

  public int getFieldSize() {
    return fieldSize;
  }

  public Map<ConstraintTypeEnum, String> getConstraints() {
    return constraints;
  }
}
