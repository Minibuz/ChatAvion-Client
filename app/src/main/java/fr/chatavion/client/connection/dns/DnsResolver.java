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
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import fr.chatavion.client.ui.ConstantKt;

/**
 * A DNS resolver for a chat client that uses MiniDNS and XBill DNS libraries for resolving DNS queries.
 */
public class DnsResolver {

    private static final Logger logger = Logger.getLogger(DnsResolver.class.getName());
    private static final int NUMBER_OF_RETRIES = 1;
    private final Base32 converter32 = new Base32();
    private Class<? extends Data> type = A.class;
    private final List<String> list = new ArrayList<>();
    private int id = 0;
    private boolean isConnected = false;

    /**
     * Constructs a new DnsResolver.
     */
    public DnsResolver() {
    }

    /**
     * Returns the ID of the DnsResolver instance.
     *
     * @return the ID of the DnsResolver instance.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the DnsResolver instance.
     * If the provided ID is negative, it will be set to 0.
     *
     * @param id the ID to set.
     */
    public void setId(int id) {
        if (id < 0) {
            this.id = 0;
            return;
        }
        this.isConnected = true;
        this.id = id;
    }

    /**
     * Checks if the current instance is connected to the network.
     *
     * @return true if the instance is connected, false otherwise.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Attempts to find the type of DNS record for a given address.
     *
     * @param address The address to resolve.
     * @return true if the type was found, false otherwise.
     */
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

    /**
     * Attempts to detect if a given community exists on a server at a given address.
     *
     * @param address   The address of the server.
     * @param community The name of the community to search for.
     * @return true if the community exists, false otherwise.
     */
    public boolean communityDetection(String address, String community) {
        try {
            ResolverResult<? extends Data> e = ResolverApi.INSTANCE.resolve(community + ".connexion." + address, type);
            isConnected = false;
            logger.info("After");
            if (!e.wasSuccessful()) {
                logger.warning(() -> "That community doesn't exist for the given server.");
                return false;
            }
            // TODO Change the return of server to get the id of the latest message receive based on the server log
            if (e.getAnswers().isEmpty()) {
                logger.warning(() -> "That community doesn't have any response.");
                return false;
            }
            for (var ip : e.getAnswers()) {
                if (A.class.equals(type)) {
                    id = Integer.parseInt(ip.toString().split("\\.")[3]);
                } else if (AAAA.class.equals(type)) {
                    id = Integer.parseInt(ip.toString().split(":")[7]);
                } else if (TXT.class.equals(type)) {
                    id = Integer.parseInt(ip.toString());
                }
            }
            isConnected = true;
            return true;
        } catch (IOException e) {
            logger.warning(() -> address + " have an issue.");
            return false;
        }
    }

    /**
     * Sends a message over DNS.
     *
     * @param community the community to send the message to
     * @param address   the DNS server address
     * @param pseudo    the username of the sender
     * @param message   the message to send
     * @return true if the message was sent successfully; false otherwise
     */
    public Boolean sendMessage(String community, String address, String pseudo, String message) {
        byte[] msgAsBytes = message.getBytes(StandardCharsets.UTF_8);
        if (msgAsBytes.length > ConstantKt.MESSAGE_SIZE) {
            logger.warning("Message cannot be more than 160 character as UTF_8 byte array.");
            return false;
        }
        String msgB32 = this.converter32.encodeAsString(msgAsBytes);
        String cmtB32 = this.converter32.encodeAsString(community.getBytes(StandardCharsets.UTF_8));
        String userB32 = this.converter32.encodeAsString(pseudo.getBytes(StandardCharsets.UTF_8));

        // TODO Generate random id for the messages
        Random r = new Random();
        int randomId = r.nextInt(65536);
        int maxSplit = (short) (msgB32.length() / 35);

        int index = 0;
        List<String> listPart = new ArrayList<>();
        for (int i = 0; i < msgB32.length() - 35; i += 35, index++) {
            String partSend = randomId + "-" +
                    maxSplit +
                    index + "-" +
                    msgB32.substring(i, i + 35);
            listPart.add(partSend);
        }
        listPart.add(randomId + "-" +
                maxSplit +
                maxSplit + "-" +
                msgB32.substring(35 * index));

        for (String msgPart : listPart) {
            for (int retries = 0; retries < NUMBER_OF_RETRIES; retries++) {
                ResolverResult<? extends Data> result;
                try {
                    result = ResolverApi.INSTANCE.resolve(
                            cmtB32 + "." + userB32 + "." + msgPart + ".message." + address, type);
                } catch (IOException e) {
                    logger.warning(() -> "Error : " + e.getMessage());
                    return false;
                }

                if (!result.wasSuccessful()) {
                    logger.warning(() -> "Server hasn't received the message.\nResending the message.");
                    continue;
                }

                if (result.getAnswers().size() == 1) {
                    logger.info(() -> "Server received the message.");
                    retries = NUMBER_OF_RETRIES;
                }
            }
        }
        return true;
    }

    /**
     * Requests the history of messages from a DNS server.
     *
     * @param cmt     the community to retrieve messages from
     * @param address the DNS server address
     * @param number  the number of messages to retrieve (between 1 and 10, inclusive)
     * @return a list of messages retrieved
     */
    public List<String> requestHistory(String cmt, String address, int number) {
        if (number < 1 || number > 10) {
            throw new IllegalArgumentException("Cannot get less than 1 message from history or more than 10.");
        }
        String cmtB32 = this.converter32.encodeAsString(cmt.getBytes(StandardCharsets.UTF_8));
        list.clear();

        try {
            for (int i = 0; i < number; i++) {
                var doRetrieve = true;
                var part = 0;
                String message = "";
                do {
                    String request = type == TXT.class ? "m" + id : type == AAAA.class ? "m" + id + "o" + part : "m" + id + "n" + part;

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
                    var fullMessageWithId = new String(converter32.decode(ArrayUtils.toPrimitive(msg.toArray(new Byte[0]))));
                    System.out.println(fullMessageWithId);
                    if (fullMessageWithId.startsWith("0")) {
                        message += fullMessageWithId.substring(1);
                        if ("".equals(message)) {
                            return list;
                        }
                        id++;
                        list.add(message);
                        doRetrieve = false;
                    } else if (fullMessageWithId.startsWith("1")) {
                        message += fullMessageWithId.substring(1);
                        part++;
                    }
                } while (doRetrieve);
            }
        } catch (IOException e) {
            return list;
        }
        return list;
    }

    /**
     * Merges the contents of a Set of A objects into a List of Byte objects.
     *
     * @param results a Set of A objects to be merged
     * @param msg     the List of Byte objects to merge the results into
     */
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

    /**
     * Merges the contents of a Set of AAAA objects into a List of Byte objects.
     *
     * @param results a Set of AAAA objects to be merged
     * @param msg     the List of Byte objects to merge the results into
     */
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

    /**
     * Merges the contents of a Set of TXT objects into a List of Byte objects.
     *
     * @param results a Set of TXT objects to be merged
     * @param msg     the List of Byte objects to merge the results into
     */
    private static void mergeResultTypeTXT(Set<TXT> results, List<Byte> msg) {
        for (var result : results) {
            var element = result.toString().replace("\"", "");
            for (int i = 0; i < result.length() - 2; i += 2) {
                msg.add(Byte.parseByte(element.substring(i, i + 2)));
            }
        }
    }
}
