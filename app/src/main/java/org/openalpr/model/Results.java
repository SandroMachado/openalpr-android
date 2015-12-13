package org.openalpr.model;

import java.util.ArrayList;

public class Results {

    private final Float epoch_time;

    private final Float processing_time_ms;

    private final ArrayList<Result> results;

    public Results(Float epoch_time, Float processing_time_ms, ArrayList<Result> results) {
        this.epoch_time = epoch_time;
        this.processing_time_ms = processing_time_ms;
        this.results = results;
    }

    public Float getEpoch_time() {
        return epoch_time;
    }

    public Float getProcessing_time_ms() {
        return processing_time_ms;
    }

    public ArrayList<Result> getResults() {
        return results;
    }

}
