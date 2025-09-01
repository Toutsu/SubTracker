package com.subtracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SubTrackerApplication

fun main(args: Array<String>) {
    runApplication<SubTrackerApplication>(*args)
}