import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ConsumerMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscription;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.concurrent.CompletableFuture;

public class AkkaKafkaStream {



    private static ConsumerSettings<String, String> getConsumerSettings(ActorSystem system){

        return  ConsumerSettings.create(system , new StringDeserializer() ,new StringDeserializer())
                .withBootstrapServers("localhost:9092")
                .withGroupId("g1")
                .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    }



    private static void plainSourceConsumer (ActorSystem system , Materializer materializer){


        Consumer.plainSource(
                getConsumerSettings(system)
                , (Subscription) Subscriptions.topics("first")

//                ,Subscriptions.assignment(new TopicPartition)
        ).mapAsync(1 , record -> {


            ConsumerRecord consumerRecord = (ConsumerRecord)record;

            System.out.println(" Th recoded in Partition ==>> "+consumerRecord.partition() +"  Key ==>> "+consumerRecord.key() + "  Value ==>> "+consumerRecord.value());

            return CompletableFuture.completedFuture(Done.getInstance());
        }).runWith(Sink.ignore() ,materializer);


    }


    /**
     * Commit each message to kafka
     * @param system
     */
    public static void committableSourceConsumer(ActorSystem system ,Materializer materializer){

        Consumer.committableSource( getConsumerSettings(system) ,  Subscriptions.topics("first"))
                .mapAsync(1 , record ->{

                    System.out.println(" Th recoded in Partition ==>> "+record.record().partition() +"  Key ==>> "+record.record().key() + "  Value ==>> "+record.record().value());


                    if (record.record().value().equals("ali"))
                        throw new RuntimeException("Ali XX!!");
                    // simulate async call for ele processing
                    CompletableFuture<Done> doneCompletableFuture = CompletableFuture.completedFuture(Done.getInstance());

                    return doneCompletableFuture.thenApply(done -> record);


                } )
                .mapAsync(1 , record -> record.committableOffset().commitJavadsl())

                .runWith(Sink.ignore() ,materializer);


    }


    public static void committableSourceWithBatch(ActorSystem system , Materializer materializer){

        Consumer.committableSource( getConsumerSettings(system) , (Subscription) Subscriptions.topics("first"))
                .mapAsync(1 , record ->{

                    System.out.println(" Th recoded in Partition ==>> "+record.record().partition() +"  Key ==>> "+record.record().key() + "  Value ==>> "+record.record().value());


                    if (record.record().value().equals("Ahmed"))
                        throw new RuntimeException("Ahmed XX!!");

                    // simulate async call for ele processing
                    CompletableFuture<Done> doneCompletableFuture = CompletableFuture.completedFuture(Done.getInstance());

                    return doneCompletableFuture.thenApply(done -> record.committableOffset());


                } )
                .batch(10 ,  first -> {


                            ConsumerMessage.CommittableOffsetBatch batch = ConsumerMessage.emptyCommittableOffsetBatch().updated(first);

                            System.out.printf("the offsets is ");
                            System.out.println(batch.getOffsets());

                            return batch;
                            }
                            , (batch, elem) ->{
//                            System.out.println("The element is "+ elem.toString());
//
//                            ConsumerMessage.CommittableOffsetBatch batch2 = batch.updated(elem);
//
//                            System.out.printl n("the batch in Fn2 == >>" +batch2);
                            return batch;

                        } )

                .mapAsync(3 ,  c -> c.commitJavadsl())
                .runWith(Sink.ignore() , materializer);


    }









    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("sys");

        Materializer materializer = ActorMaterializer.create(system);


//        plainSourceConsumer(system ,materializer);

//        committableSourceConsumer(system ,materializer);
        committableSourceWithBatch(system ,materializer);

    }
}

