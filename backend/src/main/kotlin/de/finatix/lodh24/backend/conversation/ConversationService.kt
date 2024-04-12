package de.finatix.lodh24.backend.conversation

import de.finatix.lodh24.backend.ai.ChatbotService
import de.finatix.lodh24.backend.conversation.db.Conversation
import de.finatix.lodh24.backend.conversation.db.ConversationRepository
import de.finatix.lodh24.backend.conversation.db.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class ConversationService {

    @Autowired
    private lateinit var chatbotService: ChatbotService

    @Autowired
    private lateinit var conversationRepository: ConversationRepository

    fun startConversation(): UUID {
        val conversation = conversationRepository.save(Conversation())
        val conversationToken = conversation.token

        return conversationToken
    }

    fun sendMessage(conversationToken: UUID, message: String): String {
        val conversation = conversationRepository.findById(conversationToken).orElseThrow()
        val response = generateResponse(message, conversation)

        val userMessage = Message(
            sender = "User",
            text = message,
            timestamp = getCurrentTimestamp(),
            conversation = conversation.token
        )
        val responseMessage = Message(
            sender = "AI",
            text = response,
            timestamp = getCurrentTimestamp(),
            conversation = conversation.token
        )
        conversation.messages.add(userMessage)
        conversation.messages.add(responseMessage)
        conversationRepository.save(conversation)

        return response
    }

    fun getConversation(conversationToken: UUID): Conversation {
        val conversation = conversationRepository.findById(conversationToken).orElseThrow()

        return Conversation(conversationToken, conversation.messages)
    }

    private fun generateResponse(message: String, conversation: Conversation): String {
        val answer = chatbotService.generateResponse(message,conversation)
        return answer.result.output.content
    }

    private fun getCurrentTimestamp(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        return currentDateTime.format(formatter)
    }
}