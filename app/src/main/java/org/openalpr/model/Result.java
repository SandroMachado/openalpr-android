package org.openalpr.model;

import java.util.List;

/**
 * Result model.
 */
public class Result {

    private final String plate;

    private final Double confidence;

    private final Double matchesTemplate;

    private final String region;

    private final Double regionConfidence;

    private final Double processingTimeMs;

    private final List<Coordinate> coordinates;

    private final List<Candidate> candidates;

    public Result(String plate, Double confidence, Double matchesTemplate, String region, Double regionConfidence, Double processingTimeMs, List<Coordinate> coordinates, List<Candidate> candidates) {
        this.plate = plate;
        this.confidence = confidence;
        this.matchesTemplate = matchesTemplate;
        this.region = region;
        this.regionConfidence = regionConfidence;
        this.processingTimeMs = processingTimeMs;
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
        return matchesTemplate;
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
        return regionConfidence;
    }

    /**
     * Gets the processing time.
     *
     * @return The processing time.
     */
    public Double getProcessingTimeMs() {
        return processingTimeMs;
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
