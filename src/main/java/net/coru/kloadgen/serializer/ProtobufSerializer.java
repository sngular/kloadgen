package net.coru.kloadgen.serializer;

import com.google.protobuf.DynamicMessage;
import io.confluent.kafka.schemaregistry.protobuf.dynamic.DynamicSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.protobuf.ProtobufData;
import org.apache.avro.protobuf.ProtobufDatumWriter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class ProtobufSerializer<T extends EnrichedRecord>  implements Serializer<T> {
    private static final byte MAGIC_BYTE = 0x0;
    private static final int idSize = 4;

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            byte[] result = null;

            if (data != null) {
                log.debug("data='{}'", data);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(MAGIC_BYTE);
                byteArrayOutputStream.write(ByteBuffer.allocate(idSize).putInt(data.getSchemaMetadata().getId()).array());
                BinaryEncoder binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
                ProtobufData protobufData = ProtobufData.get();
                DatumWriter<DynamicMessage> datumWriter = new ProtobufDatumWriter<>(protobufData.getSchema(((DynamicMessage)data.getGenericRecord()).getDescriptorForType()));
                datumWriter.write((DynamicMessage) data.getGenericRecord(), binaryEncoder);

                binaryEncoder.flush();
                byteArrayOutputStream.close();

                result = byteArrayOutputStream.toByteArray();
                log.debug("serialized data='{}'", DatatypeConverter.printHexBinary(result));
            }
            return result;
        } catch (IOException ex) {
            throw new SerializationException(
                    "Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
        }
    }

    @Override
    public byte[] serialize(String topic, Headers headers, T data) {
        return serialize(topic,  data);
    }

}

