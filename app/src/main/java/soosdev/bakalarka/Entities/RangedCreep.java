package soosdev.bakalarka.Entities;

import java.util.ArrayList;
import java.util.List;

import soosdev.bakalarka.Board;
import soosdev.bakalarka.Position;
import soosdev.bakalarka.R;
import soosdev.bakalarka.Tile;

/**
 * Created by patrik on 11.3.2018.
 */

public class RangedCreep extends Creep {
    public RangedCreep(Board board, Position position) {
        super(board, position);
        points = 3;
    }

    @Override
    public int attack() {
        board.addAnimationToList(animateRangedAttack(this.getPosition(), board.getPlayer().getPosition()),200);
        return board.getPlayer().takeDamage();
    }

    @Override
    public List<Tile> getRange(Tile tile) {
        ArrayList<Tile> tilesInRange = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            tilesInRange.addAll(board.getStar(tile, i));
        }
        return tilesInRange;
    }

    @Override
    public String getName() {
        return "RangedCreep";
    }

    @Override
    public int getImageResource() {
        return R.drawable.ranged;
    }
}
