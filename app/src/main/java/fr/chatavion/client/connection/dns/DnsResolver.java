package fr.chatavion.client.connection.dns;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.ArrayUtils;
import org.minidns.hla.ResolverApi;
import org.minidns.hla.ResolverResult;
import org.minidns.record.A;
import org.minidns.record.AAAA;
import org.minidns.record.Data;
import org.minidns.record.TXT;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DnsResolver {

    private static final Logger logger = Logger.getLogger(DnsResolver.class.getName());
    private static final int NUMBER_OF_RETRIES = 1;
    private final Base32 converter32 = new Base32();
    private Class<? extends Data> type = A.class;
    private final List<String> list = new ArrayList<>();
    private int id = 0;

    public DnsResolver() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean findType(String address) {
        ResolverResult<? extends Data> result;
        address = "chat." + address;
        try {
            result = ResolverApi.INSTANCE.resolve(address, TXT.class);
            if (result.wasSuccessful() && !result.getAnswers().isEmpty()) {
                logger.info(result.getAnswers().toArray()[0].toString());
                type = TXT.class;
                return true;
            }
        } catch (IOException e) {
            logger.warning(() -> "TXT bug.");
        }
        try {
            result = ResolverApi.INSTANCE.resolve(address, AAAA.class);
            if (result.wasSuccessful() && !result.getAnswers().isEmpty()) {
                logger.info(result.getAnswers().toArray()[0].toString());
                type = AAAA.class;
                return true;
            }
        } catch (IOException e) {
            logger.warning(() -> "AAAA bug.");
        }
        try {
            result = ResolverApi.INSTANCE.resolve(address, A.class);
            if (result.wasSuccessful() && !result.getAnswers().isEmpty()) {
                logger.info(result.getAnswers().toArray()[0].toString());
                type = A.class;
                return true;
            }
        } catch (IOException e) {
            logger.warning(() -> "A bug.");
        }
        return false;
    }

    public boolean communityDetection(String community, String address) {
        try {
            ResolverResult<? extends Data> e = ResolverApi.INSTANCE.resolve(community + ".connexion." + address, type);
            logger.info("After");
            if (!e.wasSuccessful()) {
                logger.warning(() -> "That community doesn't exist for the given server.");
                return false;
            }
            // TODO Change the return of server to get the id of the latest message receive based on the server log
            return !e.getAnswers().isEmpty();
        } catch (IOException e) {
            logger.warning(() -> address + " have an issue.");
            return false;
        }
    }

    public boolean sendMessage(String community, String address, String pseudo, String message) {
        byte[] msgAsBytes = message.getBytes(StandardCharsets.UTF_8);
        if (msgAsBytes.length > 35) {
            logger.warning(() -> "Message cannot be more than 35 character as UTF_8 byte array.");
            return false;
        }
        String msgB32 = this.converter32.encodeAsString(msgAsBytes);
        String cmtB32 = this.converter32.encodeAsString(community.getBytes(StandardCharsets.UTF_8));
        String userB32 = this.converter32.encodeAsString(pseudo.getBytes(StandardCharsets.UTF_8));

        for (int retries = 0; retries < NUMBER_OF_RETRIES; retries++) {
            ResolverResult<? extends Data> result;
            try {
                result = ResolverApi.INSTANCE.resolve(
                        cmtB32 + "." + userB32 + "." + msgB32 + ".message." + address, type);
            } catch (IOException e) {
                logger.warning(() -> "Error : " + e.getMessage());
                return false;
            }

            if (!result.wasSuccessful()) {
                logger.info(() -> "Server hasn't received the message.\nResending the message.");
                continue;
            }

            if (result.getAnswers().size() == 1) {
                logger.info(() -> "Server received the message.");
                return true;
            }
        }
        return false;
    }

    public List<String> requestHistory(String cmt, String address, int number) {
        if (number < 1 || number > 10) {
            throw new IllegalArgumentException("Cannot get less than 1 message from history or more than 10.");
        }
        String cmtB32 = this.converter32.encodeAsString(cmt.getBytes(StandardCharsets.UTF_8));

        list.clear();
        try {
            for (int i = 0; i < number; i++) {
                String request = type == A.class ? "m" + id : type == AAAA.class ? "m" + id + "o0" : "m" + id + "n0";

                ResolverResult<? extends Data> result = ResolverApi.INSTANCE.resolve(request + "-" + cmtB32 + ".historique." + address, type);
                if (!result.wasSuccessful()) {
                    logger.warning(() -> "Problem with recovering history.");
                    return List.of();
                }
                if (result.getAnswers().isEmpty()) {
                    logger.info(() -> "No message with this id to retrieve. Stopping message recovery.");
                    return list;
                }

                List<Byte> msg = new ArrayList<>();
                if (type == A.class) {
                    Set<A> answers = (Set<A>) result.getAnswers();
                    mergeResultTypeA(answers, msg);
                } else if (type == AAAA.class) {
                    Set<AAAA> answers = (Set<AAAA>) result.getAnswers();
                    mergeResultTypeAAAA(answers, msg);
                } else {
                    Set<TXT> answers = (Set<TXT>) result.getAnswers();
                    mergeResultTypeTXT(answers, msg);
                }
                String message = new String(converter32.decode(ArrayUtils.toPrimitive(msg.toArray(new Byte[0]))));
                id++;
                list.add(message);
            }
        } catch (IOException e) {
            return list;
        }

        return list;
    }

    private static void mergeResultTypeA(Set<A> results, List<Byte> msg) {
        // Sort results based on first byte
        HashMap<Integer, List<Byte>> map = new HashMap<>();
        for (var result : results) {
            var part = result.toString().split("\\.");
            map.put(Integer.parseInt(part[0]),
                    List.of(Byte.parseByte(part[1]),
                            Byte.parseByte(part[2]),
                            Byte.parseByte(part[3])));
        }
        List<Integer> keys = map.keySet().stream().sorted().collect(Collectors.toList());
        for (var key : keys) {
            msg.addAll(Objects.requireNonNull(map.get(key)));
        }
    }

    private static void mergeResultTypeAAAA(Set<AAAA> results, List<Byte> msg) {
        for (var result : results) {
            var part = result.toString().split(":");
            msg.add(Byte.parseByte(part[0]));
            msg.add(Byte.parseByte(part[1]));
            msg.add(Byte.parseByte(part[2]));
            msg.add(Byte.parseByte(part[3]));
            msg.add(Byte.parseByte(part[4]));
            msg.add(Byte.parseByte(part[5]));
            msg.add(Byte.parseByte(part[6]));
            msg.add(Byte.parseByte(part[7]));
        }
    }

    private static void mergeResultTypeTXT(Set<TXT> results, List<Byte> msg) {
        for (var result : results) {
            var element = result.toString().replace("\"", "");
            for (int i = 0; i < result.length() - 2; i += 2) {
                msg.add(Byte.parseByte(element.substring(i, i + 2)));
            }
        }
    }
}
