package fr.chatavion.client.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CommunityWithMessages(
    @Embedded val community: Community,
    @Relation(
        entity = Message::class,
        parentColumn = "communityId",
        entityColumn = "communityId"
    )
    val messages: List<Message>
)