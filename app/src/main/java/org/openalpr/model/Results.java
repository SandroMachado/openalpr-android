package org.openalpr.model;

import java.util.List;

public class Results {

    private final Double epoch_time;

    private final Double processing_time_ms;

    private final List<Result> results;

    public Results(Double epoch_time, Double processing_time_ms, List<Result> results) {
        this.epoch_time = epoch_time;
        this.processing_time_ms = processing_time_ms;
        this.results = results;
    }

    public Double getEpochTime() {
        return epoch_time;
    }

    public Double getProcessingTimeMs() {
        return processing_time_ms;
    }

    public List<Result> getResults() {
        return results;
    }

}
