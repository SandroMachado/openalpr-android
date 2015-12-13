package org.openalpr.model;

/**
 * Coordinate model.
 */
public class Coordinate {

    private final Integer x;

    private final Integer y;

    public Coordinate(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the X coordinate.
     *
     * @return The X coordinate.
     */
    public Integer getX() {
        return x;
    }

    /**
     * Gets the Y coordinate.
     *
     * @return the Y coordinate.
     */
    public Integer getY() {
        return y;
    }

}
