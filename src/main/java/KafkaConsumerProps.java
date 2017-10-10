import java.util.Properties;

/**
 * Created by ahmed on 10/10/17.
 */
public class KafkaConsumerProps {

    public static Properties getKafkaProps(){
        final Properties props = new Properties();
        props.put("bootstrap.servers" ,"localhost:9092");
//        props.put("retries", 0);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "test");
        props.put("enable.auto.commit", false);

        return props;
    }
}
