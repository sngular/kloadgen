/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;

import java.util.Properties;

@Slf4j
public class KafkaSampler extends AbstractKafkaSampler {

    public KafkaSampler() {
        super();
    }

    @Override
    public Arguments getDefaultParameters() {
        return SamplerUtil.getCommonDefaultParameters();
    }

    @Override
    protected Properties properties(JavaSamplerContext context) {
        Properties props = SamplerUtil.setupCommonProperties(context);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "net.coru.kloadgen.serializer.AvroSerializer");
        log.debug("Populated properties: {}", props);
        return props;
    }

    @Override
    protected Logger logger() {
        return log;
    }

}
