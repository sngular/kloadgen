package com.sngular.kloadgen.extractor.model;

import java.util.Collections;
import java.util.Map;

public interface AsyncApiAbstract {

  Object[] getProperties();

  default Map<String, String> getPropertiesMap() {
    return Collections.emptyMap();
  }

}
