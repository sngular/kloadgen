package com.sngular.kloadgen.sampler;

import java.io.Serializable;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

@Slf4j
@NoArgsConstructor
public class AsyncApiSampler extends AbstractSampler implements Serializable  {

  @Override
  public boolean applies(final ConfigTestElement configElement) {
    return super.applies(configElement);
  }

  @Override
  public SampleResult sample(final Entry entry) {
    return null;
  }
}
