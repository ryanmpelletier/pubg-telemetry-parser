package com.github.ryanp102694.pubgtelemetryparser.service;


import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class PredictionClient {
    private final static Logger log = LoggerFactory.getLogger(PredictionClient.class);


    @Value("${prediction.server.url}")
    private String predictionServerUrl;

    public String makePrediction(String jsonPredictionRequest){
        log.debug("Asking for prediction with {} to {}", jsonPredictionRequest, predictionServerUrl);

        try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(predictionServerUrl);
            StringEntity params = new StringEntity(jsonPredictionRequest);
            params.setContentType("application/json");
            request.addHeader("Content-Type", "application/json");
            request.setEntity(params);
            CloseableHttpResponse response = httpClient.execute(request);
            return IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());

        } catch (Exception ex) {
            log.debug("Could not get prediction with {} for {}", jsonPredictionRequest, predictionServerUrl);
            ex.printStackTrace();
        }
        return null;
    }

}
