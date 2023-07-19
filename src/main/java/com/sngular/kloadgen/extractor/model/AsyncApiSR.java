package com.sngular.kloadgen.extractor.model;

import lombok.Data;
import lombok.Value;

@Value
@Data
public class AsyncApiSR {

  String schemaRegistryUrl;

  String schemaRegistryVendor;
}
