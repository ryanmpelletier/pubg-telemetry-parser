package com.github.ryanp102694.pubgtelemetryparser;

import com.github.ryanp102694.pubgtelemetryparser.event.TelemetryEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BatchTelemetryProcessor {

    private final static Logger log = LoggerFactory.getLogger(BatchTelemetryProcessor.class);

    private Map<String, TelemetryEventHandler> telemetryEventHandlerMap;


    public BatchTelemetryProcessor(Map<String, TelemetryEventHandler> telemetryEventHandlerMap){
        this.telemetryEventHandlerMap = telemetryEventHandlerMap;
    }

    //will build several TelemetryParsers then process them on separate threads
    public void process(String inputDir, String outputDir){
        File folder = new File(inputDir);
        File[] listOfFiles = folder.listFiles();

        List<TelemetryProcessor> telemetryProcessors = new ArrayList<>();


        if(listOfFiles != null){
            for(int i = 0; i < listOfFiles.length; i++){
                if (listOfFiles[i].isFile()) {
                    log.debug("Found file " + listOfFiles[i].getAbsoluteFile().getName());
                    telemetryProcessors.add(new TelemetryProcessor(listOfFiles[i].getAbsoluteFile().getPath(), outputDir, telemetryEventHandlerMap));
                }
            }
        }else{
            log.error("No files found in " + inputDir + ", not processing.");
        }

        ExecutorService executor = Executors.newFixedThreadPool(8);


        long startTime = System.currentTimeMillis();
        log.info("Begin processing " + telemetryProcessors.size() + " telemetry files!");
        for(TelemetryProcessor telemetryProcessor : telemetryProcessors){
            executor.execute(telemetryProcessor);
        }
        //don't accept any more tasks
        executor.shutdown();
        try {
            //wait for the current tasks to finish
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            log.info("End processing " + telemetryProcessors.size() + " telemetry files in " + (System.currentTimeMillis() - startTime) + " milliseconds");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
