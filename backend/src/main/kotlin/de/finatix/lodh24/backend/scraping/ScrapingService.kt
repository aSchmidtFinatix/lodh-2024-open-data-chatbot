package de.finatix.lodh24.backend.scraping

import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.net.URL
import java.util.logging.Logger

@Service
class ScrapingService {

    private val logger = Logger.getLogger(ScrapingService::class.java.name)

    @EventListener(ApplicationReadyEvent::class)
    fun queryCatalogues(): List<DataSet> {
        val accessUrls = mutableListOf<DataSet>()
        Cities.entries.forEach { city ->
            val model = getCatalogueModel(city.dataCatalogue)
            accessUrls.addAll(extractDatasets(model, city))
        }
        return accessUrls
    }

    private fun getCatalogueModel(dataCatalogue: String): Model {
        val url = URL(dataCatalogue)
        val connection = url.openConnection()
        connection.connect()
        logger.info("Connection open for: $dataCatalogue")
        val input = connection.getInputStream()
        val model = Rio.parse(input, "UTF-8", RDFFormat.TURTLE)
        input.close()

        return model
    }

    private fun extractDatasets(model: Model, city: Cities): List<DataSet> {
        val dataSets = mutableListOf<DataSet>()
        model.forEach { statement: Statement ->
            val predicate = statement.predicate.stringValue()
            if (predicate == "http://purl.org/dc/terms/format") {
                val mediaType = statement.`object`.stringValue().lowercase()
                println(mediaType)
                if (!mediaType.contains("zip")) {
                    val dataSet = model.filter { it.subject == statement.subject }
                        .firstOrNull { it.predicate.stringValue() == "http://www.w3.org/ns/dcat#accessURL" }?.`object`?.stringValue()
                    if (dataSet != null) {
                        when {
                            mediaType.contains("csv") -> {dataSets.add(DataSet(url = dataSet, city = city, format = Format.CSV))}
                            mediaType.contains("xls") -> {dataSets.add(DataSet(url = dataSet, city = city, format = Format.XLS))}
                            mediaType.contains("xlsx") -> {dataSets.add(DataSet(url = dataSet, city = city, format = Format.XLSX))}
                            else -> {
                                //skip
                            }
                        }
                    }
                }
            }
        }
        return dataSets
    }
}