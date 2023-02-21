package fr.chatavion.client.ui.view

import androidx.compose.runtime.snapshots.SnapshotStateList
import fr.chatavion.client.connection.Message
import fr.chatavion.client.connection.MessageStatus
import fr.chatavion.client.connection.dns.DnsResolver
import fr.chatavion.client.connection.http.HttpResolver
import kotlin.streams.toList

fun dnsHistoryRetrieval(
    dnsResolver: DnsResolver,
    community: String,
    address: String,
    messages: SnapshotStateList<Message>
) {
    historyRetrieval(
        dnsResolver.requestHistory(
            community,
            address,
            10
        ),
        messages
    )
}

fun httpHistoryRetrieval(
    httpResolver: HttpResolver,
    community: String,
    address: String,
    messages: SnapshotStateList<Message>
) {
    historyRetrieval(
        httpResolver.requestHistory(
            community,
            address,
            10
        ),
        messages
    )
}

private fun historyRetrieval(
    history: List<String>,
    messages: SnapshotStateList<Message>
) {
    val msgList = history.stream().map { element ->
        val parts = element.split(":::")
        Message(MessageStatus.RECEIVED, parts[0], parts[1], false)
    }.toList()

    val list = messages.stream().filter { e ->
        e.status == MessageStatus.SEND
    }.toList()

    val listToRemove: MutableList<Message> = mutableListOf()
    msgList.forEach { msg ->
        for (message in list) {
            if (msg.user == message.user && msg.message == message.message) {
                msg.send = true
                listToRemove.add(message)
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