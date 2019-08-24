package com.github.ryanp102694.pubgtelemetryparser.controller;
//TODO: this will provide an API which allows you to give it a telemetry file URL and get back data needed to make ML predictions


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PubgPredictionController {

    @GetMapping("/")
    String getString(){
        return "Hello World";
    }

}
