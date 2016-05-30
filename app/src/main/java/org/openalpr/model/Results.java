package org.openalpr.model;

import java.util.List;

public class Results {

    private final Double epochTime;

    private final Double processingTimeMs;

    private final List<Result> results;

    public Results(Double epochTime, Double processingTimeMs, List<Result> results) {
        this.epochTime = epochTime;
        this.processingTimeMs = processingTimeMs;
        this.results = results;
    }

    public Double getEpochTime() {
        return epochTime;
    }

    public Double getProcessingTimeMs() {
        return processingTimeMs;
    }

    public List<Result> getResults() {
        return results;
    }

}
