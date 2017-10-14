import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Function;
import akka.japi.pf.PFBuilder;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class AkkaStreamErrorRecovery {

    static Source<Integer, NotUsed> source;


    static {
        source = Source.from(Arrays.asList(0,1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    // using  recover
    private static void recover(Materializer materializer){

        source.map(integer ->{
            if ( integer < 5) return integer.toString();

            else throw  new RuntimeException("Ex !!") ;

        } ).recover(
                new PFBuilder()
                .match(RuntimeException.class , ex -> "Stream Error !!")
                .build()
        ).runForeach(System.out::println ,materializer);


    }


    /**
     *  recoverWithRetries allows you to put a new upstream in place of the failed one,
     *  recovering stream failures up to a specified maximum number of times.
     * @param materializer
     */
    private static void recoverWithRetries(Materializer materializer){

        Source<String, NotUsed> planB = Source.from(Arrays.asList("one", "two", "three", "four"));

        source.map(integer -> {
            if (integer < 5 ) return integer.toString();

            else  throw new RuntimeException("Stream Error!!!");
        }).recoverWithRetries(2 ,
                new PFBuilder()
                .match(RuntimeException.class , ex -> planB)
                .build()

        ).runForeach(System.out::println ,materializer);


    }

    private static void matWithSupervisionStr(ActorSystem system){

        /// the decider
        final akka.japi.function.Function<Throwable,Supervision.Directive> decider = ex ->{
            if (ex instanceof ArithmeticException )
                return Supervision.resume();

            else return Supervision.stop();

        };

        final Materializer materializer = ActorMaterializer.create(
                ActorMaterializerSettings.create(system).withSupervisionStrategy(decider),system);


        Source<Integer, NotUsed> source1 = source.map(integer -> {
            System.out.println("the num is ==" +integer);
            return 100 / integer; // this result division by zero exception (ArithmeticException )  if we didn't handle it
        });

       final Sink<Integer, CompletionStage<Integer>> sink = Sink.<Integer, Integer>fold(0, (acc, element) -> acc + element);

        CompletionStage<Integer> integerCompletionStage = source1.runWith(sink, materializer);

        integerCompletionStage.thenAccept(integer -> System.out.println("The final result is ======>>> "+integer));


    }



    private static void flowWithSupervisionStr(Materializer materializer){
        /// the decider
        final akka.japi.function.Function<Throwable,Supervision.Directive> decider = ex ->{
            if (ex instanceof ArithmeticException )
                return Supervision.resume();

            else return Supervision.stop();

        };

        Flow<Integer, Integer, NotUsed> flow =
                                    Flow.of(Integer.class)
                                    .filter(elem -> 100 / elem < 50)
                                    .map(elem -> 100 / (5 - elem))
                                    .withAttributes(ActorAttributes.withSupervisionStrategy(decider));
//                                    .withAttributes(ActorAttributes.withSupervisionStrategy(Supervision.getResumingDecider())); // the same as above

        final Source<Integer, NotUsed> source1 = Source.from(Arrays.asList(0, 1, 2, 3, 4, 5))
                .via(flow);


        final Sink<Integer, CompletionStage<Integer>> fold =
                Sink.fold(0, (acc, elem) -> acc + elem);

        final CompletionStage<Integer> result = source1.runWith(fold, materializer);

        result.thenAccept(integer -> System.out.println("Final Result is ==== >> "+integer));



    }



    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("Sys");

        Materializer materializer = ActorMaterializer.create(system);

        // 1)
//            System.out.println(" ================== Recover ==================");
//             recover(materializer);

        // 2)
//            System.out.println(" ================== Recover With Retries ==================");
//            recoverWithRetries(materializer);


        // 3)
//            System.out.println(" ================== Materializer Actor with SupervisionStrategy  ==================");
//            matWithSupervisionStr(system);


        //4)
            flowWithSupervisionStr(materializer);






    }


}
