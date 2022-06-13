package net.coru.kloadgen.processor.objectcreator;

import java.util.ArrayDeque;

import net.coru.kloadgen.common.ProcessorFieldTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;

public interface ProcessorObjectCreator {

  Object createObjectChain(ArrayDeque<FieldValueMapping> fieldExpMappingsQueue);
}
