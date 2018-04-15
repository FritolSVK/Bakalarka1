package soosdev.bakalarka;

/**
 * Created by patrik on 9.3.2018.
 */

public enum Direction {
    TOP("U"),
    TOPRIGHT("UR"),
    BOTTOMRIGHT("DR"),
    BOTTOM("D"),
    BOTTOMLEFT("DL"),
    TOPLEFT("UL");


    private final String direction;

    Direction(final String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return direction;
    }
}
