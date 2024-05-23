package de.finatix.lodh24.backend.scraping

enum class Cities(val dataCatalogue: String) {
    LEIPZIG("https://opendata.leipzig.de/catalog.ttl"),
    HAMBURG("https://suche.transparenz.hamburg.de/catalog.ttl"),
    //MUNICH("https://opendata.muenchen.de/catalog.ttl"),
    BERLIN("https://datenregister.berlin.de/catalog.ttl")
}