package fr.chatavion.client.ui.view

import android.util.Log
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
        Log.i("test", element)
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
    if(list.isNotEmpty()) {
        // TODO Toast here to main thread
        // CoroutineScope(Main) {}
        for(msg in list) {
            if(msg.sendRetry == 0) {
                listToRemove.remove(msg)
            } else {
                msg.sendRetry --
            }
        }
        // listToRemove.addAll(list)
    }

    messages.removeAll(
        listToRemove
    )
    messages.addAll(
        msgList
    )
}