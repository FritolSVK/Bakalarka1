package soosdev.bakalarka.Entities;

import java.util.ArrayList;
import java.util.List;

import soosdev.bakalarka.Board;
import soosdev.bakalarka.Position;
import soosdev.bakalarka.R;
import soosdev.bakalarka.Tile;

/**
 * Created by patrik on 18.3.2018.
 */

public class Temple extends Entity{

    private boolean wasActivated = false;

    public Temple(Board board, Position position) {
        super(board, position);
    }

    @Override
    public List<Tile> getRange() {
        return board.getNeighbours(board.getTiles().get(position.getTag()));
    }

    @Override
    public String getName() {
        return "Temple";
    }

    public void changeState() {
        wasActivated = !wasActivated;
    }

    public boolean isActivated() {
        return wasActivated;
    }

    @Override
    public int getImageResource() {
        return R.drawable.temple;
    }
}
