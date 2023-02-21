package fr.chatavion.client.ui.view

import androidx.compose.runtime.snapshots.SnapshotStateList
import fr.chatavion.client.connection.dns.DnsResolver
import fr.chatavion.client.connection.http.HttpResolver
import fr.chatavion.client.db.entity.Message
import fr.chatavion.client.db.entity.MessageStatus
import kotlin.streams.toList

fun dnsHistoryRetrieval(
    dnsResolver: DnsResolver,
    community: String,
    address: String,
    messages: SnapshotStateList<Message>,
    communityId: Int
) {
    historyRetrieval(
        dnsResolver.requestHistory(
            community,
            address,
            10
        ),
        messages,
        communityId
    )
}

fun httpHistoryRetrieval(
    httpResolver: HttpResolver,
    community: String,
    address: String,
    messages: SnapshotStateList<Message>,
    communityId: Int
) {
    historyRetrieval(
        httpResolver.requestHistory(
            community,
            address,
            10
        ),
        messages,
        communityId
    )
}

private fun historyRetrieval(
    history: List<String>,
    messages: SnapshotStateList<Message>,
    communityId: Int
) {
    val msgList = history.stream().map { element ->
        val parts = element.split(":::")
        Message(parts[0], parts[1], communityId, MessageStatus.RECEIVED, false)
    }.toList()

    val list = messages.stream().filter { e ->
        e.status == MessageStatus.SEND
    }.toList()

    val listToRemove: MutableList<Message> = mutableListOf()
    msgList.forEach { msg ->
        for (message in list) {
            if (msg.pseudo == message.pseudo && msg.message == message.message) {
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