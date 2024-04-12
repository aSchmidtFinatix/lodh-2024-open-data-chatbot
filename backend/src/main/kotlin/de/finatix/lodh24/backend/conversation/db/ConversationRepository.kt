package de.finatix.lodh24.backend.conversation.db

import org.springframework.data.repository.CrudRepository
import java.util.*

interface ConversationRepository : CrudRepository<Conversation, UUID> {
}