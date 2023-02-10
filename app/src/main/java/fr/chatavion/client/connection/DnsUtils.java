package fr.chatavion.client.connection;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DnsUtils {

    public static Response forNameType(Resolver resolver, String nameStr, int type) throws UnknownHostException {
        Name name;

        try {
            name = Name.fromString(nameStr + ".");
        } catch (TextParseException e) {
            throw new IllegalArgumentException();
        }
        Lookup lookup = null;
        try {
            lookup = new Lookup(Name.fromString("message.chatavion.com."), type, DClass.ANY);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
        lookup.setResolver(resolver);
        lookup.setCache(null);
        lookup.run();

        int result = lookup.getResult();
        if (lookup.getResult() == Lookup.HOST_NOT_FOUND) {
            System.out.println("Not found.");
            return new Response(result, List.of());
        } else if (lookup.getResult() == Lookup.TRY_AGAIN) {
            System.out.println("Error!1");
            return new Response(result, List.of());
        }  else if (lookup.getResult() == Lookup.TYPE_NOT_FOUND) {
            System.out.println("Error!2");
            return new Response(result, List.of());
        } else if (lookup.getResult() == Lookup.UNRECOVERABLE) {
            System.out.println("Error!3");
            return new Response(result, List.of());
        }
        return new Response(result, Arrays.stream(lookup.getAnswers())
                .map(Record::rdataToString)
                .collect(Collectors.toList()));
//        Resolver r = new SimpleResolver("8.8.8.8");
//        Record queryRecord = Record.newRecord(name, Type.A, DClass.ANY);
//        Message query = Message.newQuery(queryRecord);
//        try {
//            r.sendAsync(query)
//                    .whenComplete(
//                            (answer, ex) -> {
//                                if (ex == null) {
//                                    System.out.println("G MARDCHE");
//                                    System.out.println(answer);
//                                } else {
//                                    System.out.println(ex);
//                                }
//                            })
//                    .toCompletableFuture()
//                    .get();
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return new Response(0, List.of());
    }
}
