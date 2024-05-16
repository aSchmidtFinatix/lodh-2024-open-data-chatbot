package de.finatix.lodh24.backend.ai

import de.finatix.lodh24.backend.conversation.db.Conversation
import de.finatix.lodh24.backend.scraping.DataSet
import org.springframework.ai.chat.ChatResponse
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

    private val options = OpenAiChatOptions.builder().withModel("gpt-3.5-turbo").build()


    fun generateResponse(message: String, conversation: Conversation): ChatResponse {
        val messages = conversation.messages.map { it.text }

        val context = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(1))

        println(context)

        val source = getSourceFromDocuments(context)

        val sysPrompt = UserMessage(
            "You are an AI assistant for the Open Data Platform of the City of Leipzig. Your role is to identify relevant data sources from SOURCE that citizens are seeking and ask clarifying questions based on the CONTEXT $. provided." +
                    "Respond to their inquiries in the original language of the MESSAGE $message. If uncertain about an answer, politely indicate that you're unable to provide it, ensuring to always disclose the SOURCE. $source Always tell the citizen that the information can be found in $source"
        )

        val userPrompt = UserMessage(
            "You've provided the following MESSAGE: $message. The CONTEXT is: . Please specify the SOURCE for your inquiry: $source."
        )

        val analysis = client.call(Prompt(listOf(sysPrompt), options))

        if (!analysis.result.output.content.contains(source)) {
            analysis.result.output.content.plus("SOURCE : $source")
        }

        // val promptText = "Here is the conversation until now. $messages Here is the new message: ${message}. Here is the knowledge that you have about that: $dataContent. Generate a useful response in the language that MESSAGE is in."


        //client.call(Prompt(promptText, options))

        return analysis
    }

    fun saveDataSet(sampleData: String, dataSet: DataSet) {
        //TODO: Fix Timeout for GPT and handle request limits

        val description = client.call("""
            Generate a description for this data in German $sampleData.The source is the city of ${dataSet.city} 
            Use also the URL to extract infos for the description. ${dataSet.url}
        """.trimIndent())

        val document = Document(sampleData, mapOf("City" to dataSet.city, "Url" to dataSet.url))
        println("__________________________")
        println(document.content)
        println(document.metadata)

        vectorStore.add(listOf(document))
    }

    private fun getSourceFromDocuments(documents: List<Document>): String  {
        val result = StringBuilder()
        for (document in documents) {
            val content = document.metadata
            val source = content["DATASOURCE"]
            result.append(source)
        }
        return result.toString()
    }
}