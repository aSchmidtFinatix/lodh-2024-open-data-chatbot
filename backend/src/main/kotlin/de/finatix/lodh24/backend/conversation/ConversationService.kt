package de.finatix.lodh24.backend.conversation

import de.finatix.lodh24.backend.ai.OpenAiService
import de.finatix.lodh24.backend.conversation.db.Conversation
import de.finatix.lodh24.backend.conversation.db.ConversationRepository
import de.finatix.lodh24.backend.conversation.db.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class PastConversation(val title: String, val id: UUID)

@Service
class ConversationService {

    @Autowired
    private lateinit var openAiService: OpenAiService

    @Autowired
    private lateinit var conversationRepository: ConversationRepository

    fun startConversation(): UUID {
        val conversation = conversationRepository.save(Conversation())
        val conversationToken = conversation.token

        return conversationToken
    }

    fun getPastConversations(tokenList: List<UUID>): List<PastConversation> {
        val savedConversations = conversationRepository.findAllById(tokenList)
        val view = mutableListOf<PastConversation>()

        savedConversations.forEach {
            view.add(PastConversation(
                id = it.token,
                title = ""
            ))
        }

        return view
    }

    fun sendMessage(conversationToken: UUID, message: String): Message {
        val conversation = conversationRepository.findById(conversationToken).orElseThrow()

        val userMessage = Message(
            sender = "User",
            text = message,
            timestamp = getCurrentTimestamp(),
            conversation = conversation.token
        )

        conversation.messages.add(userMessage)

        val response = generateResponse(message, conversation)


        val responseMessage = Message(
            sender = "AI",
            text = response,
            timestamp = getCurrentTimestamp(),
            conversation = conversation.token
        )
        conversation.messages.add(responseMessage)
        conversationRepository.save(conversation)

        return responseMessage
    }

    fun getConversation(conversationToken: UUID): Conversation {
        val conversation = conversationRepository.findById(conversationToken).orElseThrow()

        return Conversation(conversationToken, conversation.messages)
    }

    private fun generateResponse(message: String, conversation: Conversation): String {
        return openAiService.generateResponse(message,conversation)
    }

    private fun getCurrentTimestamp(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        return currentDateTime.format(formatter)
    }
}