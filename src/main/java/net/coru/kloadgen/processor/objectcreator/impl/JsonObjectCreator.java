package net.coru.kloadgen.processor.objectcreator.impl;

import java.util.ArrayDeque;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coru.kloadgen.common.ProcessorFieldTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreator;
import net.coru.kloadgen.randomtool.generator.StatelessGeneratorTool;

public class JsonObjectCreator implements ProcessorObjectCreator {

  private static final ObjectMapper mapper = new ObjectMapper();

  private StatelessGeneratorTool statelessGeneratorTool;

  public JsonObjectCreator() {
    this.statelessGeneratorTool = new StatelessGeneratorTool();
  }

  @Override
  public Object createObjectChain(FieldValueMapping fieldValueMapping) {
    switch(objectType) {
      case RECORD_MAP_MAP:

        return null;
      case RECORD_ARRAY_ARRAY:

        return null;
      case RECORD_MAP_ARRAY:

        return null;
      case RECORD_ARRAY_MAP:

        return null;
      case RECORD_MAP:

        return null;
      case RECORD_ARRAY:

        return null;
      case BASIC_MAP_MAP:

        return null;
      case BASIC_ARRAY_ARRAY:

        return null;
      case BASIC_MAP_ARRAY:

        return null;
      case BASIC_ARRAY_MAP:

        return null;
      case BASIC_MAP:

        return null;
      case BASIC_ARRAY:

        return null;
      case BASIC:

        return null;
      case FINAL:

        return null;
      default:

        return null;
    }
  }
}
