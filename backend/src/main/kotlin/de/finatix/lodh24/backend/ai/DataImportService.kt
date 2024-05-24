package de.finatix.lodh24.backend.ai

import de.finatix.lodh24.backend.scraping.DataSet
import de.finatix.lodh24.backend.scraping.Format
import de.finatix.lodh24.backend.scraping.ScrapingService
import kotlinx.coroutines.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.logging.Logger


data class DataRecord(val values: Map<String, String>)

@Service
@ConditionalOnProperty("spring.dataimport.enabled")
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
                    val dataSetSpec = generateSpec(dataSet)
                    if (!dataSetSpec.isNullOrEmpty()) {
                        openAiService.saveDataSet(dataSetSpec, dataSet)
                    }
                }
            }

            jobs.forEach { it.join() }
            logger.info("Import Done is done!")
        }
    }

    private suspend fun generateSpec(dataSet: DataSet): String? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(dataSet.url).openConnection()
                connection.connect()

                connection.getInputStream().use { inputStream ->
                    return@withContext when (dataSet.format) {
                        Format.XLS, Format.XLSX -> processExcel(inputStream)
                        Format.CSV -> processCSV(inputStream)
                    }
                }
            } catch (e: Exception) {
                logger.warning("Error: ${e.message}")
                null
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