package de.finatix.lodh24.backend.conversation.db

import jakarta.persistence.*
import java.util.*
@Entity
data class Message (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID = UUID.randomUUID(),

    val sender: String = "",

    val text: String = "",

    val timestamp: String = "",

    val conversation: UUID
)


