package de.finatix.lodh24.backend

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller("/")
class
ViewController {
    @GetMapping
    fun getIndex(): String {
        return "Index"
    }
}