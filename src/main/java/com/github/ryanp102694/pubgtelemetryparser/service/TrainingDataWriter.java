package com.github.ryanp102694.pubgtelemetryparser.service;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
public class TrainingDataWriter {

    @Value("${data.output.dir}")
    private String dataOutputDir;

    private final static Logger log = LoggerFactory.getLogger(TrainingDataWriter.class);

    @Async
    public CompletableFuture<Void> writeGameDataPoints(String matchUUID, Map<String, SortedMap<String, String>> playerDataPoints) {
        log.debug("Writing game training data for {}", matchUUID);
        try{
            BufferedWriter trainingDataWriter = new BufferedWriter(new FileWriter(dataOutputDir + "/" + matchUUID + ".training.csv"));
            BufferedWriter trainingLabelWriter = new BufferedWriter(new FileWriter(dataOutputDir + "/" + matchUUID + ".labels.csv"));

            List<SortedMap<String, String>> dataPointsList = new ArrayList<>(playerDataPoints.values());

            //these strings should be built at the same time
            trainingDataWriter.write(getTrainingDataString(dataPointsList));
            trainingLabelWriter.write(getLabelsString(dataPointsList));

            trainingDataWriter.close();
            trainingLabelWriter.close();
        }catch(IOException e){
            log.debug("Unable to write game training data for {}", matchUUID);
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    //I want a method very similar to this for getting my prediction request JSON
    private String getTrainingDataString(List<SortedMap<String, String>> dataPointsList){
        StringBuilder trainingDataBuilder = new StringBuilder();

        for(int i = 0; i < dataPointsList.size(); i++){

            SortedMap<String, String> dataPoints = dataPointsList.get(i);

            for(String key : dataPoints.keySet()){
                if(!"alive".equals(key)){
                    trainingDataBuilder.append(dataPoints.get(key)).append(",");
                }
            }
            trainingDataBuilder.setLength(trainingDataBuilder.length() - 1);
            //add a newline if there are more datapoints
            if(i != dataPointsList.size() - 1){
                trainingDataBuilder.append("\n");
            }
        }
        return trainingDataBuilder.toString();
    }

    private String getLabelsString(List<SortedMap<String, String>> dataPointsList){
        StringBuilder labelBuilder = new StringBuilder();

        for(int i = 0; i < dataPointsList.size(); i++){

            SortedMap<String, String> dataPoints = dataPointsList.get(i);

            for(String key : dataPoints.keySet()){
                if("alive".equals(key)){
                    String otherString = "";
                    if("1".equals(dataPoints.get(key))){
                        otherString = "0";
                    }else{
                        otherString = "1";
                    }
                    labelBuilder.append(dataPoints.get(key)).append(",").append(otherString);
                }
            }
            //add a newline if there are more datapoints
            if(i != dataPointsList.size() - 1){
                labelBuilder.append("\n");
            }
        }
        return labelBuilder.toString();
    }

    public void mergeData(){
        try{
            log.debug("Merging training files.");
            mergeTrainingFiles();
            log.debug("Merging label files.");
            mergeLabelFiles();
            log.debug("Deleting intermediate files.");
            deleteIntermediateFiles();
        }catch(IOException e){
            log.debug("Something went wrong and you should feel bad.");
            e.printStackTrace();
        }
    }


    private void mergeTrainingFiles() throws IOException {
        mergeTrainingDataFiles("training");
    }
    private void mergeLabelFiles() throws IOException {
        mergeTrainingDataFiles("labels");

    }

    //builds a single training.csv and labels.csv
    private void mergeTrainingDataFiles(String filePattern) throws IOException {

        File folder = new File(dataOutputDir);
        List<File> sources = Stream.of(folder.listFiles())
                .filter(file -> file.getName().contains(filePattern))
                .sorted()
                .collect(Collectors.toList());

        File destination = new File(dataOutputDir + "/" + filePattern + ".csv");

        try(OutputStream output = createAppendableStream(destination)) {
            for(int i = 0; i < sources.size(); i++){
                appendFile(output, sources.get(i), i == 0);
            }
        }
    }

    private BufferedOutputStream createAppendableStream(File destination)
            throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(destination, true));
    }

    private void appendFile(OutputStream output, File source, boolean firstFile) throws IOException {
        try(InputStream input = new BufferedInputStream(new FileInputStream(source))) {
            if(!firstFile){
                IOUtils.write("\n", output, Charset.defaultCharset());
            }
            IOUtils.copy(input, output);
        }
    }

    private void deleteIntermediateFiles() {
        File folder = new File(dataOutputDir);
        Stream.of(folder.listFiles())
                .filter(file -> file.getName().contains(".training.") || file.getName().contains(".labels."))
                .forEach(File::delete);
    }

}
