package com.sngular.kloadgen.schemaregistry.adapter.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GenericSchemaRegistry<T, U> {

  private GenericSchemaRegistryAdapter genericSchemaRegistryAdapter;

  private T id;

  private T version;

  private U schemaType;

}