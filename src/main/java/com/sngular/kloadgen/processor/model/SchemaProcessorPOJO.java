package com.sngular.kloadgen.processor.model;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;

import com.sngular.kloadgen.model.ConstraintTypeEnum;
import com.sngular.kloadgen.model.FieldValueMapping;
import lombok.Builder;

@Builder
public class SchemaProcessorPOJO implements Cloneable {

  private String rootFieldName;

  private String fieldNameSubEntity;

  private String completeFieldName;

  private Integer valueLength;

  private String valueType;

  private List<String> fieldValuesList;

  private Map<ConstraintTypeEnum, String> constraints;

  private int level;

  private ArrayDeque<FieldValueMapping> fieldExpMappingsQueue;

  private String completeTypeFilterChain;

  private boolean lastFilterTypeOfLastElement;

  private int fieldSize;

  public String getRootFieldName() {
    return rootFieldName;
  }

  public ArrayDeque<FieldValueMapping> getFieldExpMappingsQueue() {
    return fieldExpMappingsQueue;
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

  @Override
  public Object clone() throws CloneNotSupportedException {
    final Object cloned = super.clone();
    ((SchemaProcessorPOJO) cloned).fieldExpMappingsQueue = this.fieldExpMappingsQueue.clone();
    return cloned;
  }
}
