package de.finatix.lodh24.backend.ai

import de.finatix.lodh24.backend.scraping.DataSet
import de.finatix.lodh24.backend.scraping.Format
import de.finatix.lodh24.backend.scraping.ScrapingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlinx.coroutines.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.logging.Logger


data class DataRecord(val values: Map<String, String>)

@Service
class DataImportService {

    @Autowired
    private lateinit var scrapingService: ScrapingService

    @Autowired
    private lateinit var openAiService: OpenAiService

    private val logger = Logger.getLogger(DataImportService::class.java.name)

    @EventListener(ApplicationReadyEvent::class)
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
        val description: String
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(dataSet.url).openConnection()
                connection.connect()

                val inputStream = connection.getInputStream()

                description = when (dataSet.format) {
                    Format.XLS, Format.XLSX -> processExcel(inputStream)
                    Format.CSV -> processCSV(inputStream)
                }
                inputStream.close()
                openAiService.saveDataSet(description, dataSet)
            } catch (e: Exception) {
                //logger.warning("Error processing URL: $url, Error: ${e.message}")
            }
        }
    }

    fun processCSV(inputStream: InputStream): String {
        val csv = CSVParser(InputStreamReader(inputStream), CSVFormat.DEFAULT.withFirstRecordAsHeader())
        val headers = csv.headerMap.keys
        val sampleRecords = csv.records.take(5).map { csvRecord ->
            DataRecord(headers.associateWith { header -> csvRecord[header] })
        }

        return prepareDataSet(headers, sampleRecords)
    }

    fun processExcel(inputStream: InputStream): String {
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)
        val headers = sheet.getRow(0).map { it.stringCellValue }.toSet()
        val sampleRecords = sheet.drop(1).take(5).map { row ->
            DataRecord(headers.associateWith { header ->
                val cellIndex = headers.indexOf(header)
                row.getCell(cellIndex)?.toString() ?: ""
            })
        }

        workbook.close()
        return prepareDataSet(headers, sampleRecords)
    }

    private fun prepareDataSet(headers: Set<String>, records: List<DataRecord>): String {
        val headerDescription = headers.joinToString(", ")
        val sampleData = records.joinToString("\n") { record ->
            headers.joinToString(", ") { header -> record.values[header] ?: "" }
        }

        return """
        CSV/Excel Description:
        Headers: $headerDescription
        Sample Data:
        $sampleData
    """.trimIndent()
    }
}