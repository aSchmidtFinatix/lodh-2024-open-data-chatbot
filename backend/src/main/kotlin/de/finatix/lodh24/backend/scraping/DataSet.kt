package de.finatix.lodh24.backend.scraping

data class DataSet(
    val url: String,
    val city: Cities,
    val format: Format
)
