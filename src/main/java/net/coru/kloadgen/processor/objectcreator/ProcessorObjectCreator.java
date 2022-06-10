package net.coru.kloadgen.processor.objectcreator;

import java.util.ArrayDeque;

import net.coru.kloadgen.common.ProcessorFieldTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;

public interface ProcessorObjectCreator {

  Object createObject(ProcessorFieldTypeEnum objectType, Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, String fieldName);
}
