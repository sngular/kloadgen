package com.sngular.kloadgen.sampler.schemaregistry.adapter.impl;

public interface GenericSchemaRegistryAdapter <T,U> {
   T getId();

   T getVersion();

   U getSchemaType();

}

