package org.openalpr.model;

import java.util.List;

/**
 * Result model.
 */
public class Result {

    private final String plate;

    private final Double confidence;

    private final Double matches_template;

    private final String region;

    private final Double region_confidence;

    private final Double processing_time_ms;

    private final List<Coordinate> coordinates;

    private final List<Candidate> candidates;

    public Result(String plate, Double confidence, Double matches_template, String region, Double region_confidence, Double processing_time_ms, List<Coordinate> coordinates, List<Candidate> candidates) {
        this.plate = plate;
        this.confidence = confidence;
        this.matches_template = matches_template;
        this.region = region;
        this.region_confidence = region_confidence;
        this.processing_time_ms = processing_time_ms;
        this.coordinates = coordinates;
        this.candidates = candidates;
    }

    /**
     * Gets the plate.
     *
     * @return the plate.
     */
    public String getPlate() {
        return plate;
    }

    /**
     * Gets the confidence.
     *
     * @return The confidence.
     */
    public Double getConfidence() {
        return confidence;
    }

    /**
     * Gets the matches template.
     *
     * @return The matches template.
     */
    public Double getMatchesTemplate() {
        return matches_template;
    }

    /**
     * Gets the region.
     *
     * @return The region.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Gets the region confidence.
     *
     * @return the region confidence.
     */
    public Double getRegionConfidence() {
        return region_confidence;
    }

    /**
     * Gets the processing time.
     *
     * @return The processing time.
     */
    public Double getProcessingTimeMs() {
        return processing_time_ms;
    }

    /**
     * Gets the coordinates.
     *
     * @return The coordinates.
     */
    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    /**
     * Gets the candidates.
     *
     * @return The candidates.
     */
    public List<Candidate> getCandidates() {
        return candidates;
    }

}
