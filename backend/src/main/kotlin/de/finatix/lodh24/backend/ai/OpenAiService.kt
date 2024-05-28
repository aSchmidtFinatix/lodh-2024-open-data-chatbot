package de.finatix.lodh24.backend.ai

import de.finatix.lodh24.backend.conversation.db.Conversation
import de.finatix.lodh24.backend.scraping.DataSet
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.ChatResponse
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.document.Document
import org.springframework.ai.openai.OpenAiChatClient
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.vectorstore.MilvusVectorStore
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OpenAiService {

    @Autowired
    private lateinit var client: OpenAiChatClient

    @Autowired
    private lateinit var vectorStore: MilvusVectorStore

    @Autowired
    private lateinit var tokenTracker: TokenTracker

    private val tokenLimitPerMinute = 16000

    private val maxTokens = 4096

    private val contextMargin = 100

    private val options = OpenAiChatOptions.builder().withModel("gpt-3.5-turbo").build()

    private val logger = LoggerFactory.getLogger(OpenAiService::class.java)

    fun generateResponse(message: String, conversation: Conversation): String {
        val messages = conversation.messages.map { it.text }

        val selectedMessages = selectChatMessages(messages)

        val query = SearchRequest.query(message)

        val context = vectorStore.similaritySearch(query)

        val source = getSourceFromDocuments(context)

        val sysPrompt = SystemMessage(
            """
                You are an AI assistant for the Open Data Platforms of the City of Leipzig,Hamburg, Berlin and Munich. 
                Your name is ODA (Open Data Assistant). Your role is to identify relevant data sources from SOURCE  
                that citizens are seeking and ask clarifying questions based on the $context provided.
                Respond to their inquiries in the original language of the MESSAGE $message. 
                If uncertain about an answer, politely indicate that you're unable to provide it but try to use documents from the Chathistory if you can.
                If you cant find any data about the inquiries state the datasets that you have with source. Always state the datasets with source.
            """.trimIndent()
        )

        val userPrompt = UserMessage(message)

        val chatMessages = selectedMessages.map { UserMessage(it) } + userPrompt + sysPrompt

        val analysis = client.call(Prompt(chatMessages, options))

        return analysis.result.output.content
    }

    fun saveDataSet(dataSpec: String, dataSet: DataSet) = runBlocking {
        val requestText = """
            Generate a description for this data in German $dataSpec.The source is the city of ${dataSet.city} 
        """.trimIndent()

        val tokensRequired = estimateTokens(requestText)
        val tokensUsed = tokenTracker.getTokensUsedInLastMinute()
        if (tokensUsed + tokensRequired > tokenLimitPerMinute) {
            logger.warn("Tokenlimit überschritten für beschreibung $requestText")
            return@runBlocking
        }
        try {
            val description = client.call(Prompt(requestText, options))
            tokenTracker.addTokens(description.metadata.usage.totalTokens)
            val document =
                Document(description.result.output.content, mapOf("City" to dataSet.city, "Url" to dataSet.url))

            vectorStore.add(listOf(document))
        } catch (e: Exception) {
            logger.info(e.message)
        }
    }

    private fun getSourceFromDocuments(documents: List<Document>): String {
        val result = StringBuilder()
        for (document in documents) {
            val content = document.metadata
            val source = content["Url"]
            result.append(source)
        }
        return result.toString()
    }

    //Based on OpenAIs Guideline you can estimate the token length with 0.75 of the texts length
    private fun estimateTokens(text: String): Int {
        return text.length / 4
    }

    private fun selectChatMessages(messages: List<String>): List<String> {
        val includedMessages = mutableListOf<String>()
        var totalTokens = 0

        for (message in messages.asReversed()) {
            val messageTokenCount = estimateTokens(message)

            if (totalTokens + messageTokenCount + contextMargin <= maxTokens) {
                includedMessages.add(message)
                totalTokens += messageTokenCount
            } else {
                break
            }
        }

        includedMessages.reverse()
        return includedMessages
    }
}