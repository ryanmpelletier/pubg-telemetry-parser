package com.github.ryanp102694.pubgtelemetryparser.controller;
//TODO: this will provide an API which allows you to give it a telemetry file URL and get back data needed to make ML predictions


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pubgml")
public class PubgPredictionController {

    @GetMapping("/prediction")
    String getString(@RequestParam("telemetryUrl") String telemetryUrl){
        //download telemetry file
        //build query arrays
        //hit tensorflow model server and get predictions
        //create response and send to client
        return "Hello World";
    }

}
