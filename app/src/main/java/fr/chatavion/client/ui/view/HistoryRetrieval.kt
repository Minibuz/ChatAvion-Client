package fr.chatavion.client.ui.view

import androidx.compose.runtime.snapshots.SnapshotStateList
import fr.chatavion.client.connection.dns.DnsResolver
import fr.chatavion.client.connection.http.HttpResolver
import fr.chatavion.client.db.entity.Message
import fr.chatavion.client.db.entity.MessageStatus
import kotlin.streams.toList

/**
 * Retrieves message history using DNS resolver
 * @param dnsResolver the DNS resolver instance used to request message history
 * @param community the community to retrieve message history from
 * @param address the address to retrieve message history from
 * @param messages the list of messages to update with retrieved message history
 */
fun dnsHistoryRetrieval(
    dnsResolver: DnsResolver,
    community: String,
    address: String,
    messages: SnapshotStateList<Message>,
    nbMessage: Int
) {
    historyRetrieval(
        dnsResolver.requestHistory(
            community,
            address,
            nbMessage
        ),
        messages
    )
}

/**
 * Retrieves message history using HTTP resolver
 * @param httpResolver the HTTP resolver instance used to request message history
 * @param community the community to retrieve message history from
 * @param address the address to retrieve message history from
 * @param messages the list of messages to update with retrieved message history
 */
fun httpHistoryRetrieval(
    httpResolver: HttpResolver,
    community: String,
    address: String,
    messages: SnapshotStateList<Message>,
    nbMessage: Int
) {
    historyRetrieval(
        httpResolver.requestHistory(
            community,
            address,
            nbMessage
        ),
        messages
    )
}

/**
 * Retrieves message history and updates the given message list.
 * @param history the list of messages to update with retrieved message history
 * @param messages the list of messages to update
 * @return nothing
 */
private fun historyRetrieval(
    history: List<String>,
    messages: SnapshotStateList<Message>
) {
    val msgList = history.stream().map { element ->
        val parts = element.split(":::")
        Message(parts[0], parts[1], MessageStatus.RECEIVED, false, 0)
    }.toList()

    val list = messages.stream().filter { e ->
        e.status == MessageStatus.SEND
    }.toList().toMutableList()


    val listToRemove: MutableList<Message> = mutableListOf()
    val listOfChanged: MutableList<Message> = mutableListOf()
    msgList.forEach { msg ->
        for (message in list) {
            if (msg.pseudo == message.pseudo && msg.message == message.message) {
                msg.send = true
                listOfChanged.add(msg)
                listToRemove.add(message)
            }
        }
    }
    list.removeAll(listOfChanged)
    if (list.isNotEmpty()) {
        // TODO Toast here to main thread
        // CoroutineScope(Main) {}
        for (msg in list) {
            if (msg.sendRetry == 0) {
                listToRemove.remove(msg)
            } else {
                msg.sendRetry--
            }
        }
    }
    messages.removeAll(
        listToRemove
    )
    messages.addAll(
        msgList
    )
}