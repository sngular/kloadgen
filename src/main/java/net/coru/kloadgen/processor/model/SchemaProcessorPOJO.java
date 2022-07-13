package net.coru.kloadgen.processor.model;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import net.coru.kloadgen.model.ConstraintTypeEnum;

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

  private ArrayDeque<?> fieldExpMappingsQueue;

  private String completeTypeFilterChain;

  private boolean lastFilterTypeOfLastElement;

  private int fieldSize;

  public String getRootFieldName() {
    return rootFieldName;
  }

  public ArrayDeque<?> getFieldExpMappingsQueue() {
    return fieldExpMappingsQueue;
  }

  public void setFieldExpMappingsQueue(final ArrayDeque<?> fieldExpMappingsQueue) {
    this.fieldExpMappingsQueue = fieldExpMappingsQueue;
  }

  public String getFieldNameSubEntity() {
    return fieldNameSubEntity;
  }

  public String getCompleteTypeFilterChain() {
    return completeTypeFilterChain;
  }

  public void setCompleteTypeFilterChain(final String completeTypeFilterChain) {
    this.completeTypeFilterChain = completeTypeFilterChain;
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

  public void setLastFilterTypeOfLastElement(final boolean lastFilterTypeOfLastElement) {
    this.lastFilterTypeOfLastElement = lastFilterTypeOfLastElement;
  }

  public int getFieldSize() {
    return fieldSize;
  }

  public void setFieldSize(final int fieldSize) {
    this.fieldSize = fieldSize;
  }

  public Map<ConstraintTypeEnum, String> getConstraints() {
    return constraints;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
