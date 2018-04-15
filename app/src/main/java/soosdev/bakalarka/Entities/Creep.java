package soosdev.bakalarka.Entities;

import java.util.ArrayList;
import java.util.List;

import soosdev.bakalarka.Board;
import soosdev.bakalarka.Position;
import soosdev.bakalarka.R;
import soosdev.bakalarka.Tile;

/**
 * Created by patrik on 8.3.2018.
 */

public class Creep extends Entity {
    protected int points;

    public Creep(Board board, Position position) {
        super(board, position);
        this.changePosition(position);
        points = 2;
    }

    public int die() {
        this.getOccupiedTile().setEntity(null);
        board.getCreeps().remove(this);
        return points;
    }

    public int attack() {
        board.addAnimationToList(animateAttackMelee(this.getPosition(), board.getPlayer().getPosition()),300);
        return board.getPlayer().takeDamage();
    }

    //can be overridden for other type of creep besides ranged/melee
    public boolean isInRange() {
        ArrayList<Tile> rangeFrom = (ArrayList<Tile>) getPlayerAttackablePatternTiles();
        for (Tile tile : rangeFrom) {
            if (position.equals(tile.getPosition())) {
                return true;
            }
        }
        return false;
    }

    public int moveOneToAttackPosition() {
        ArrayList<Tile> hasRangeTiles = (ArrayList<Tile>) getPlayerAttackablePatternTiles();
        ArrayList<ArrayList<Tile>> possiblePaths = new ArrayList<>();
        for (Tile tile : hasRangeTiles) {
            if (tile.isWalkable())
                possiblePaths.add((ArrayList<Tile>) board.findShortestPath(this.getOccupiedTile(), tile));
        }
        int pathLength = 9000;
        ArrayList<Tile> shortestPath = null;
        for (ArrayList<Tile> path : possiblePaths) {
            if (path != null)
                if (path.size() < pathLength) {
                    pathLength = path.size();
                    shortestPath = path;
                }
        }
        if (shortestPath == null)
            return -1; //there is no path
        return this.moveOneByPath(shortestPath);
    }

    public List<Tile> getRange(Tile tile) {
        return board.getNeighbours(tile);
    }

    /**
     * @return tiles to which the creep has range
     */
    public List<Tile> getRangePatternTiles() {
        //TO OVERRIDE FOR EVERY CREEP TYPE UNLESS MELEE
        return getRange(board.getTiles().get(this.getPosition().getTag()));
    }

    /**
     * @return tiles from which player can be attacked
     */
    public List<Tile> getPlayerAttackablePatternTiles() {
        //TO OVERRIDE FOR EVERY CREEP TYPE UNLESS MELEE
        return getRange(board.getTiles().get(board.getPlayer().getPosition().getTag()));
    }

    public String getName() {
        return "Creep";
    }

    @Override
    public int getImageResource() {
        return R.drawable.creep;
    }
}
