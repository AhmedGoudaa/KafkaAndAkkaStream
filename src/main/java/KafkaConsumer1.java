import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public class KafkaConsumer1 {



    private static Properties getKafkaProps(){
        final Properties props = new Properties();
        props.put("bootstrap.servers" ,"localhost:9092");
//        props.put("retries", 0);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "test");
        props.put("enable.auto.commit", false);

        return props;
    }

    public static void onComplete(){

    }

    public static void main(String[] args) {
        KafkaConsumer kafkaConsumer = new KafkaConsumer(getKafkaProps());

        kafkaConsumer.subscribe(Arrays.asList("first"));


        try {
            while (true){
                    ConsumerRecords<String ,String> consumerRecords = kafkaConsumer.poll(11);

                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {

                    System.out.println("{ Consumer consume ==>>Topic " + consumerRecord.topic() + " partition ==>> " + consumerRecord.partition()
                    +"  Offset ==>> "+ consumerRecord.offset()
                    ) ;


                    System.out.println("key " + consumerRecord.key() + " Value ==>> " + consumerRecord.value()+" }");
                }

                kafkaConsumer.commitAsync();
//                kafkaConsumer.commitAsync(new OffsetCommitCallback() {
//                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
//                        System.out.println("Kafka rocks it !! ");
//                    }
//                });


            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }finally {
            kafkaConsumer.close();
        }


    }

}
