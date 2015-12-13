package org.openalpr.app;

import java.io.Serializable;

/**
 * Created by sujay on 23/09/14.
 */
public class AlprResultItem implements Serializable {
    private String plate;
    private double confidence;
    private long processingTime; // in ms

    public AlprResultItem(){}

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }
}
