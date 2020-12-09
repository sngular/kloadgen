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

import java.util.Objects;
import java.util.Properties;

import static net.coru.kloadgen.util.ProducerKeysHelper.ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG;
import static net.coru.kloadgen.util.ProducerKeysHelper.VALUE_NAME_STRATEGY;

@Slf4j
public class ConfluentKafkaSampler extends AbstractKafkaSampler {

    public ConfluentKafkaSampler() {
        super();
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = SamplerUtil.getCommonDefaultParameters();
        arguments.addArgument(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        arguments.addArgument(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        return arguments;
    }

    @Override
    protected Properties properties(JavaSamplerContext context) {
        Properties props = SamplerUtil.setupCommonProperties(context);
        props.put(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, context.getParameter(ENABLE_AUTO_SCHEMA_REGISTRATION_CONFIG, "false"));
        if (Objects.nonNull(context.getParameter(VALUE_NAME_STRATEGY))) {
            props.put(VALUE_NAME_STRATEGY, context.getParameter(VALUE_NAME_STRATEGY));
        }
        log.debug("Populated properties: {}", props);
        return props;
    }

    @Override
    protected Logger logger() {
        return log;
    }

}
