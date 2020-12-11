package net.coru.kloadgen.sampler;

import lombok.extern.slf4j.Slf4j;
import net.coru.kloadgen.serializer.GenericAvroRecordBinarySerializer;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;

import java.util.Properties;

import static net.coru.kloadgen.util.ProducerKeysHelper.KEY_SERIALIZER_CLASS_CONFIG_DEFAULT;

@Slf4j
public class GenericBinaryKafkaSampler extends AbstractKafkaSampler {

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = SamplerUtil.getCommonDefaultParameters();
        defaultParameters.addArgument(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER_CLASS_CONFIG_DEFAULT);
        defaultParameters.addArgument(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GenericAvroRecordBinarySerializer.class.getName());
        return defaultParameters;
    }

    @Override
    protected Properties properties(JavaSamplerContext context) {
        Properties props = SamplerUtil.setupCommonProperties(context);
        log.debug("Populated properties: {}", props);
        return props;
    }

    @Override
    protected Logger logger() {
        return log;
    }

}
