package org.openalpr.model;

/**
 * Candidate model.
 */
public class Candidate {

    private final String plate;

    private final Double confidence;

    private final Integer matches_template;

    public Candidate(String plate, Double confidence, Integer matches_template) {
        this.plate = plate;
        this.confidence = confidence;
        this.matches_template = matches_template;
    }

    /**
     * Gets the plate.
     *
     * @return The plate.
     */
    public String getPlate() {
        return plate;
    }

    /**
     * Gets the confidence level.
     *
     * @return the confidence level.
     */
    public Double getConfidence() {
        return confidence;
    }

    /**
     * Gets the matches template.
     *
     * @return the matches template.
     */
    public Integer getMatchesTemplate() {
        return matches_template;
    }

}
