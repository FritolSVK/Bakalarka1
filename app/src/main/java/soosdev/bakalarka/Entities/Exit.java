package soosdev.bakalarka.Entities;

import java.util.List;

import soosdev.bakalarka.Board;
import soosdev.bakalarka.Position;
import soosdev.bakalarka.R;
import soosdev.bakalarka.Tile;

/**
 * Created by patrik on 19.3.2018.
 */

public class Exit extends Entity {

    public Exit(Board board, Position position) {
        super(board, position);
    }

    @Override
    public String getName() {
        return "Exit";
    }

    @Override
    public List<Tile> getRange() {
        return board.getNeighbours(board.getTiles().get(position.getTag()));
    }

    @Override
    public int getImageResource() {
        return R.drawable.exit;
    }
}
