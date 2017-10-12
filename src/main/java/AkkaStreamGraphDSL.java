import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.*;
import akka.stream.javadsl.*;
import scala.Int;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by ahmed on 10/12/17.
 */
public class AkkaStreamGraphDSL {

    /**
     *                        --------------------<---------
 *                           |                              |
 *                           |                              |
 *                            ->
     *                            C -------                 |
     *                       ->            |                ^
     *                    |                |                |
     *                    |                |                |
     *                    |                |                |
     *                    |                |                |
     *                 ---                 |                |
     *                ^                     ---->           |
     *                |                              F  -->--
     * A(Source)----> B                    ------>
     *                |                  |
     *                V                  ^
     *                |                  |
     *                 ->-               |
     *                    |           ->-
     *                     -> D ->  E
     *                                ->-
     *                                   |
     *                                   V
     *                                    --->----> G (Sink)
     * @param args
     */


    public static void main(String[] args) {

        final ActorSystem system =ActorSystem.create("sys");

        final Materializer  materializer = ActorMaterializer.create(system);

        RunnableGraph<NotUsed> runnableGraph = RunnableGraph.fromGraph(
                GraphDSL.create(
                        builder ->
                        {
                            // the first stage (A)
                            final Outlet<Integer> source = builder.add(Source.from(Arrays.asList(1,2,3,4,5,6,7,8))).out();
                            final UniformFanOutShape<Integer, Integer> B = builder.add(Broadcast.create(2));
                            final UniformFanInShape<Integer, Integer> C = builder.add(Merge.create(2));
                            final FlowShape<Integer, Integer> D = builder.add(Flow.of(Integer.class).map(i -> i ));

                            final UniformFanOutShape<Integer, Integer> E = builder.add(Broadcast.create(2));
                            final UniformFanInShape<Integer, Integer> F = builder.add(Merge.create(2));

                            // the final stage (G)
                            final Inlet<Integer> sink = builder.add(Sink.<Integer>foreach((i)->System.out.println("the I ===> "+i))).in();


                            // attache the graph components
                            // all port should be all connected with any way


                            //solution 1
                            builder.from(source).viaFanOut(B)
                                    .via(D).viaFanOut(E).viaFanIn(F).toFanIn(C);
                            builder.from(B).viaFanIn(C).toFanIn(F);
                            builder.from(E).toInlet(sink);

                            //solution 2
//                            builder.from(F).toFanIn(C);
//                            builder.from(source).viaFanOut(B).viaFanIn(C).toFanIn(F);
//                            builder.from(B).via(D).viaFanOut(E).toFanIn(F);
//                            builder.from(E).toInlet(sink);


                            return ClosedShape.getInstance();
                        }
                )
        );

        runnableGraph.run(materializer);

        System.out.println("materializedCombiner ---------------VV");

        materializedCombiner(materializer);

    }

    public static void materializedCombiner(Materializer  materializer){

//        Source<Integer, CompletableFuture<Optional<Integer>>> source = Source.range(1 ,12);

        Source<Integer, NotUsed> source = Source.range(1, 12);
        


//        Flow<Integer, Integer, NotUsed> flow =
        Flow<Integer, String, NotUsed> flow =
                Flow.of(Integer.class).take(100).map((i) -> Integer.valueOf(i).toString());


//        Source<Integer, NotUsed> source1 = 
        Source<String, NotUsed> source1 =
                source.viaMat(flow, Keep.right());


        Source<String, NotUsed> source2 =
                source.viaMat(flow, Keep.left());


//        Source<Integer, Pair<CompletableFuture<Optional<Integer>>, NotUsed>> source3 = 
        Source<String, Pair<NotUsed, NotUsed>> source3 =
                source.viaMat(flow, Keep.both());


        source1.runWith(Sink.foreach(System.out::println) , materializer);
        source2.runWith(Sink.foreach(System.out::println) , materializer);
        source3.runWith(Sink.foreach(System.out::println) , materializer);

    }
}
