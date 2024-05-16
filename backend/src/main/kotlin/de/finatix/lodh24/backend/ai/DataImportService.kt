package de.finatix.lodh24.backend.ai

import de.finatix.lodh24.backend.scraping.DataSet
import de.finatix.lodh24.backend.scraping.Format
import de.finatix.lodh24.backend.scraping.ScrapingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlinx.coroutines.*
import java.io.InputStream
import java.net.URL
import java.util.logging.Logger


@Service
class DataImportService {

    @Autowired
    private lateinit var scrapingService: ScrapingService

    private val logger = Logger.getLogger(DataImportService::class.java.name)

    @ExperimentalCoroutinesApi
    fun importDataFromCatalogue() {
        val dataSets: List<DataSet> = scrapingService.queryCatalogues()

        runBlocking {
            val jobs = dataSets.map { dataSet ->
                launch {
                    processUrl(dataSet)
                }
            }

            jobs.forEach { it.join() }
        }
    }

    private suspend fun processUrl(dataSet: DataSet) {
        logger.info("Processing URL: $dataSet")
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(dataSet.url).openConnection()
                connection.connect()

                val inputStream = connection.getInputStream()

                when (dataSet.format) {
                    Format.XLS, Format.XLSX -> processExcel(inputStream)
                    Format.CSV -> processCSV(inputStream)
                }

                inputStream.close()
            } catch (e: Exception) {
                //logger.warning("Error processing URL: $url, Error: ${e.message}")
            }
        }
    }

    private fun processExcel(inputStream: InputStream) {

    }

    private fun processCSV(inputStream: InputStream) {
        // Process CSV file
        // Example: Use BufferedReader to read CSV file
        logger.info("$inputStream is an CSV")
    }
}