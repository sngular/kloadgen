package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

import java.lang.invoke.SwitchPoint;
import java.util.Properties;

import com.sngular.kloadgen.util.SchemaRegistryKeyHelper;
import org.apache.jmeter.testelement.property.JMeterProperty;

public class SchemaRegistryFactory {

private Properties properties;
  String schemaRegistryName = this.properties.getProperty(SchemaRegistryKeyHelper.SCHEMA_REGISTRY_NAME);

switch(schemaRegistryName){
    case "APICURIO":
      break;
    case 'CONFLUENT':
      break;

}}
