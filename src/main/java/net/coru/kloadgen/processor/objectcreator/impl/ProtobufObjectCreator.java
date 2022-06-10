package net.coru.kloadgen.processor.objectcreator.impl;

import java.io.IOException;
import java.util.ArrayDeque;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.coru.kloadgen.common.ProcessorFieldTypeEnum;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.processor.ProtoBufProcessorHelper;
import net.coru.kloadgen.processor.objectcreator.ProcessorObjectCreator;
import net.coru.kloadgen.randomtool.generator.ProtoBufGeneratorTool;
import net.coru.kloadgen.randomtool.random.RandomMap;
import net.coru.kloadgen.randomtool.random.RandomObject;


public class ProtobufObjectCreator extends ProtoBufProcessorHelper implements ProcessorObjectCreator {

  private Descriptors.Descriptor schema;

  private SchemaMetadata metadata;

  private RandomObject randomObject;

  private RandomMap randomMap;

  private ProtoBufGeneratorTool generatorTool;

  public ProtobufObjectCreator(Object schema, SchemaMetadata metadata) throws DescriptorValidationException, IOException {
    if(schema instanceof ParsedSchema) {
      this.schema = new ProtoBufProcessorHelper().buildDescriptor((ProtoFileElement) ((ParsedSchema)schema).rawSchema());
    }
    else if(schema instanceof ProtoFileElement) {
      this.schema = new ProtoBufProcessorHelper().buildDescriptor((ProtoFileElement)schema);
    }
    this.metadata = metadata;
    this.randomObject = new RandomObject();
    this.randomMap = new RandomMap();
    this.generatorTool = new ProtoBufGeneratorTool();
  }

  @Override
  public Object createObject(final ProcessorFieldTypeEnum objectType, Object entity, ArrayDeque<FieldValueMapping> fieldExpMappingsQueue, final String fieldName) {
    switch(objectType) {
      case OBJECT_MAP_MAP:

        return null;
      case OBJECT_ARRAY_ARRAY:

        return null;
      case OBJECT_MAP_ARRAY:

        return null;
      case OBJECT_ARRAY_MAP:

        return null;
      case OBJECT_MAP:

        return null;
      case OBJECT_ARRAY:

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
