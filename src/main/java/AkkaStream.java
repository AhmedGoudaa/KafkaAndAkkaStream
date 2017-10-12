import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;

/**
 * Created by ahmed on 10/11/17.
 */
public class AkkaStream {


    public static void main(String[] args) {

        final ActorSystem system =ActorSystem.create("sys");

        final Materializer  materializer = ActorMaterializer.create(system);

        Source<Integer , NotUsed> source = Source.range(1,100);


        final Source<BigInteger, NotUsed> factorials =
                source
                        .scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));


//        source.map(param -> ByteString.fromString(param.toString())+"\n")
//                .runWith(FileIO.toPath(Paths.get("Factorials")) ,materializer) ;


        factorials.runForeach(param -> System.out.println(param) ,materializer);
        source.runForeach(i -> System.out.println("i ==>> "+i), materializer);

//         Flow.of(String.class)
//                .map(param -> ByteString.fromString(param.toString()) + "\n")
//                .toMat(FileIO.toPath(Paths.get("file.txt")), Keep.right());
//

    }
}
