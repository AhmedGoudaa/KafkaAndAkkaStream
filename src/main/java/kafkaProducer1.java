import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by ahmed on 10/10/17.
 */
public class kafkaProducer1
{


    private Properties getKafkaProps(){
        final Properties props = new Properties();
        props.put("bootstrap.servers" ,"localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return props;
    }


    public static void main(String[] args) throws InterruptedException {

        kafkaProducer1 kafkaProducer1 = new kafkaProducer1();
        Producer<String, String> producer = new KafkaProducer<String, String>(kafkaProducer1.getKafkaProps());


        for (int i = 0; i <10000000 ; i++) {
            producer.send(new ProducerRecord<String, String>("first" ,Integer.toString(i), Integer.toString(i) ));
            System.out.println("Producer produce ==>> "+Integer.toString(i));
            Thread.sleep(500);
        }

        producer.close();




    }
}

