package soosdev.bakalarka.Entities;

import java.util.List;

import soosdev.bakalarka.Board;
import soosdev.bakalarka.Position;
import soosdev.bakalarka.R;
import soosdev.bakalarka.Tile;

/**
 * Created by patrik on 30.3.2018.
 */

public class Goliath extends Creep {

    private boolean wasHit = false;

    public Goliath(Board board, Position position) {
        super(board, position);
        points = 5;
    }

    @Override
    public String getName() {
        return "Goliath";
    }

    @Override
    public int die() {
        if (wasHit)
            return super.die();
        else
            wasHit = true;
        return 0;
    }

    public boolean wasHit() {
        return wasHit;
    }

    @Override
    public int getImageResource() {
        return R.drawable.goliath;
    }
}
