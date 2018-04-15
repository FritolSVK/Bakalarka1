package soosdev.bakalarka.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import soosdev.bakalarka.Board;
import soosdev.bakalarka.Position;
import soosdev.bakalarka.R;
import soosdev.bakalarka.Tile;

/**
 * Created by patrik on 8.3.2018.
 */

public class Player extends Entity{

    private int maxLives = 1;
    private int maxPillumCount = 1;
    private int health = 1;//2
    private int pillumCount = 1;
    private int cooldownTime = 5;
    private int currentCooldownTime = 0;
    private int throwRange = 2;
    private boolean shielded = false;
    private int score = 0;
    private int shieldedTime;
    private int maxShieldedTime;
    private HashMap<String, Boolean> abilities;

    public Player(Board board, Position position) {
        super(board, position);
        createAbilities();
    }

    public int attackFirst() {

        Creep toBeAttacked = getOneToBeAttacked();
            if (toBeAttacked != null) {
                board.addAnimationToList(animateAttackMelee(this.getPosition(), toBeAttacked.getPosition()),300);
                increaseScore(toBeAttacked.die());
            }
        return 0;
    }

    public Creep getOneToBeAttacked() {
        for (Tile neighbour : this.board.getNeighbours(board.getTiles().get(position.getTag()))) {
            if (neighbour.getEntity() != null && !(neighbour.getEntity() instanceof Temple) && !(neighbour.getEntity() instanceof Exit))
                return (Creep)neighbour.getEntity();
        }
        return null;
    }

    public List<Tile> getPathToPoint(Tile to) {
        return board.findShortestPath(board.getTiles().get(position.getTag()), to);
    }

    public List<Tile> getThrowRangeTiles() {
        ArrayList<Tile> range = new ArrayList<>();
        for (Tile tile : board.getFilledCircle(board.getTiles().get(this.getPosition().getTag()), throwRange)) {
            if (tile.isWalkable())
                range.add(tile);
        }
        return range;
    }

    private boolean checkIsInRange() {
        for (Creep creep : board.getCreeps()) {
            if (creep.isInRange())
                return true;
        }
        return false;
    }

    public int takeDamage() {
        if (!shielded) {
            --health;
            board.createLivesImages();
            return health;
        }
        else
            return health;
    }

    public int getHealth() {
        return health;
    }

    public int getPillumCount() {
        return pillumCount;
    }

    private void usePillum() {
        pillumCount -= 1;
        board.createStaminaImages();
    }

    public void increaseMaxHealth() {
        if (!abilities.get("Health1")) {
            maxLives++;
            health++;
            abilities.put("Health1", true);
        } else {
            if (!abilities.get("Health2")) {
                maxLives++;
                health++;
                abilities.put("Health2", true);
            } else {
                return;
            }
        }
        board.createLivesImages();
    }

    public void increasePillumCount() {
        if (!abilities.get("Pillum1")) {
            maxPillumCount += 1;
            pillumCount += 1;
            abilities.put("Pillum1", true);
        } else {
            if (!abilities.get("Pillum2")) {
                maxPillumCount += 1;
                pillumCount += 1;
                abilities.put("Pillum2", true);
            } else {
                return;
            }
        }
        board.createStaminaImages();
    }

    public void heal() {
        health = maxLives;
        board.createLivesImages();
    }

    public void pickUpPillums() {
        pillumCount = maxPillumCount;
        board.createStaminaImages();
    }

    private void createAbilities() {
        abilities = new HashMap<>();
        abilities.put("Health1", false);
        abilities.put("Health2", false);
        abilities.put("Pillum1", false);
        abilities.put("Pillum2", false);
        abilities.put("Shielderino", false);
        abilities.put("Throwerino", false);
    }

    @Override
    public String getName() {
        return "Player";
    }

    public HashMap<String, Boolean> getAbilities() {
        return abilities;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public void setMaxLives(int maxLives) {
        this.maxLives = maxLives;
    }

    public int getMaxPillumCount() {
        return maxPillumCount;
    }

    public void setMaxPillumCount(int maxPillumCount) {
        this.maxPillumCount = maxPillumCount;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setPillumCount(int pillumCount) {
        this.pillumCount = pillumCount;
    }

    public void setCooldownTime(int cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public int getCooldownTime() {
        return cooldownTime;
    }

    public void resetCooldown() {
        currentCooldownTime = cooldownTime;
        shieldedTime = maxShieldedTime;}

    public int getThrowRange() {
        return throwRange;
    }

    public void setThrowRange(int throwRange) {
        this.throwRange = throwRange;
    }

    public void shieldCooldown() {
        if (currentCooldownTime > 0)
            currentCooldownTime--;
        shieldedTime--;
        if (currentCooldownTime == 0) {
            shielded = false;
        }
    }

    public void nullCooldown() {
        currentCooldownTime = 0;
    }

    public void increaseThrowRange() {
        abilities.put("Throwerino", true);
        throwRange++;
    }

    public void reduceCooldown() {
        abilities.put("Shielderino", true);
        cooldownTime--;
    }

    public void throwPillum(Tile tile) {
        board.addAnimationToList(animateRangedAttack(getPosition(), tile.getPosition()),200);
        if (tile.getEntity() instanceof Creep) {
            increaseScore(((Creep) tile.getEntity()).die());
        }
        usePillum();
    }


    public int getCurrentCooldownTime() {
        return currentCooldownTime;
    }

    public boolean isShielded() {
        return shielded;
    }

    public void setShielded(boolean shielded) {
        this.shielded = shielded;
    }

    @Override
    public int getImageResource() {
        return R.drawable.player;
    }

    public void increaseScore(int i) {
        score += i;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
