import org.apache.kafka.clients.consumer.*;

import java.util.Arrays;

public class KafkaConsumer1 {

    public static void main(String[] args) {
        KafkaConsumer kafkaConsumer = new KafkaConsumer(KafkaConsumerProps.getKafkaProps());

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
