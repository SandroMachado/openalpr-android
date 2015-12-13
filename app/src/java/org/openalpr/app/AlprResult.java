package org.openalpr.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sujay on 23/09/14.
 */
public class AlprResult implements Serializable {
    private long epoch;
    private long processingTime; // in ms
    private boolean recognized = true;

    private List<AlprResultItem> resultItems;
    private List<AlprCandidate> candidates;


    public AlprResult(){
        resultItems = new ArrayList<AlprResultItem>();
        candidates = new ArrayList<AlprCandidate>();
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public List<AlprResultItem> getResultItems() {
        return resultItems;
    }

    public List<AlprCandidate> getCandidates() {
        return candidates;
    }

    public boolean isRecognized() {
        return recognized;
    }

    public void setRecognized(boolean recognized) {
        this.recognized = recognized;
    }

    public void addResultItem(AlprResultItem resultItem){
        resultItems.add(resultItem);
    }

    public void addCandidate(AlprCandidate candidate){
        candidates.add(candidate);
    }
}
