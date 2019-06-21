package com.github.ryanp102694.pubgtelemetryparser.data;

import java.util.function.Function;

public class RangeMapper implements Function<Double, Double> {

    private double inputLowerBound;
    private double inputUpperBound;
    private double outputLowerBound;
    private double outputUpperBound;


    public RangeMapper(double inputLowerBound, double inputUpperBound, double outputLowerBound, double outputUpperBound){
        this.inputLowerBound= inputLowerBound;
        this.inputUpperBound = inputUpperBound;
        this.outputLowerBound = outputLowerBound;
        this.outputUpperBound = outputUpperBound;
    }


    @Override
    public Double apply(Double input) {
        return ((input - inputLowerBound) * ((outputUpperBound - outputLowerBound)/(inputUpperBound - inputLowerBound))) + outputLowerBound;
    }
}
