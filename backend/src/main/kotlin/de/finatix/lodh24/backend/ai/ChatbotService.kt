package de.finatix.lodh24.backend.ai

import de.finatix.lodh24.backend.conversation.db.Conversation
import org.springframework.ai.chat.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.document.Document
import org.springframework.ai.openai.OpenAiChatClient
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.vectorstore.MilvusVectorStore
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ChatbotService {

    @Autowired
    private lateinit var client: OpenAiChatClient

    @Autowired
    private lateinit var vectorStore: MilvusVectorStore

    fun generateResponse(message: String, conversation: Conversation): ChatResponse {
        val context = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(1))

        val contentList = getContentOfDocuments(context)

        val messagesText = conversation.messages.joinToString(separator = ", ") { it.text }


        val promptText = "Here is the conversation until now. $messagesText Here is the new message: ${message}. Here is the knowledge that you have about that: $contentList. Generate a useful response in the language that MESSAGE is in."

        val options = OpenAiChatOptions.builder().withModel("gpt-3.5-turbo").build()

        return client.call(Prompt(promptText, options))
    }

    private fun getContentOfDocuments(documents: List<Document>): List<String> {
        val contentList = mutableListOf<String>()
        for (document in documents) {
            val content = document.content
            contentList.add(content)
        }
        return contentList
    }
}