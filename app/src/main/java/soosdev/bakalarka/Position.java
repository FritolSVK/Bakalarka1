package soosdev.bakalarka;

/**
 * Created by patrik on 2.3.2018.
 */

public class Position {
    private int x,y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position pos) {
        this.x = pos.getX();
        this.y = pos.getY();
    }

    public Position clone() {
        return new Position(this);
    }

    public String toString() {
        return String.format("%03d\n%03d", x, y);
    }

    public String getTag() {
        return String.format("%03d%03d", x, y);
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Position position) {
        this.x = position.getX();
        this.y = position.getY();
    }

    public int getX() { return x; }

    public int getY() {
        return y;
    }

    public boolean equals(Position other) {
        return this.x == other.x && this.y == other.y;
    }

}
