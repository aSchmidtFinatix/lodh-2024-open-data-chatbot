package de.finatix.lodh24.backend.conversation.db

import jakarta.persistence.*
import java.util.UUID

@Entity
data class Conversation(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val token: UUID = UUID.randomUUID(),

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "conversation")
    val messages: MutableList<Message> = mutableListOf()
)