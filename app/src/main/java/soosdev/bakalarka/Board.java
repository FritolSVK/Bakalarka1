package soosdev.bakalarka;

import android.animation.AnimatorSet;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import soosdev.bakalarka.Entities.Creep;
import soosdev.bakalarka.Entities.Entity;
import soosdev.bakalarka.Entities.Exit;
import soosdev.bakalarka.Entities.Goliath;
import soosdev.bakalarka.Entities.Player;
import soosdev.bakalarka.Entities.RangedCreep;
import soosdev.bakalarka.Entities.Temple;

/**
 * Created by patrik on 28.2.2018.
 */

public class Board extends AppCompatActivity implements AbilityDialog.AbilityOptions {
    private RelativeLayout layout;
    private int width, height;
    private final int nrOfRows = 10;
    private final int nrOfColumns = 11;
    private HashMap<String, Tile> tiles;
    private ArrayList<Creep> creeps = new ArrayList<Creep>();
    private Player player;
    private Temple temple;
    private Exit exit;
    private Tile currentlySelected = null;
    private final Board board = this;
    private boolean throwing = false;
    private int level = 1;
    private boolean aiControl = false;
    private ArrayList<AnimatorSet> animations = new ArrayList<>();
    private ArrayList<Integer> animationsLengths = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_activity);
        layout = findViewById(R.id.boardLayout);
        getSupportActionBar().hide();

        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                width = layout.getMeasuredWidth();
                height = layout.getMeasuredHeight();
                if (width > 0 && height > 0)
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                createBoard();
                createTilesControl();
                createEntities();
                loadMap(level);
                if (getIntent().getBooleanExtra("continue", false)) {
                    loadGame();
                }
                createLivesImages();
                createStaminaImages();
                playAnimations(true);
            }
        });
    }

    private void createTilesControl() {

        createBoardTouch();

        createAbilitiesButtons();
    }

    private void createBoardTouch() {
        for (final Tile tile : tiles.values()) {
            tile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (aiControl) return;
                    if (throwing) {
                        if (throwPillum(tile)) return;
                    }
                    if ( ( currentlySelected == null || currentlySelected != tile ) && tile.isWalkable() ) {
                        createEntitiesControl(tile);
                    } else {
                        moveAttack(tile);
                    }
                }
            });
        }
    }

    private void moveAttack(Tile tile) {
        if (currentlySelected.getEntity() == null && currentlySelected.isWalkable()) {
            resetSelection();
            ArrayList<Tile> path = (ArrayList<Tile>) player.getPathToPoint(tile);
            if (path == null) {
                showFinalScore("no_turn");
            }
            player.moveOneByPath(player.getPathToPoint(tile));
            player.attackFirst();
            makeAiTurns();
        }
    }

    private void createEntitiesControl(Tile tile) {
        resetSelection();
        //select tile and show what can be done/show range of the creep
        currentlySelected = tile;
        Entity entity = currentlySelected.getEntity();
        if (entity != null) {
            if (entity instanceof Creep) {
                //show range
                ArrayList<Tile> range = (ArrayList<Tile>) ((Creep) entity).getRangePatternTiles();
                highlightRange(range);
            }
            if (entity instanceof Temple && containsPlayerWithinRange(temple)) {
                if (!temple.isActivated())
                    createUpgradeMenu();
                currentlySelected = null;
            }
            if (entity instanceof Exit && containsPlayerWithinRange(exit)) {
                nextLevel();
            }
        } else {
            //show path
            ArrayList<Tile> path = (ArrayList<Tile>) player.getPathToPoint(tile);
            highlightPath(path);
        }
    }

    private boolean throwPillum(Tile tile) {
        if (player.getThrowRangeTiles().contains(tile) && currentlySelected == tile) {
            player.throwPillum(tile);
            ((ToggleButton) findViewById(R.id.throwBtn)).setChecked(false);
            throwing = false;
            makeAiTurns();
            return true;
        } else {
            if (player.getThrowRangeTiles().contains(tile)) {
                showThrowRange();
                tile.setBackgroundResource(R.drawable.tilerangehighlight);
                currentlySelected = tile;
                return true;
            }
        }
        return false;
    }

    private void createAbilitiesButtons() {
        ((ToggleButton) findViewById(R.id.throwBtn)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (aiControl) return;
                throwing = b;
                resetSelection();
                if (throwing) {
                    if (player.getPillumCount() >= 1)
                        showThrowRange();
                    else {
                        throwing = false;
                        ((ToggleButton) findViewById(R.id.throwBtn)).setChecked(false);
                    }
                } else {
                    resetSelection();
                }
            }
        });

        ((Button)findViewById(R.id.shieldBtn)).setText("SHIELD");
        ((Button)findViewById(R.id.shieldBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (aiControl) return;
                    if (player.getCurrentCooldownTime() == 0)
                        player.setShielded(true);
                        player.resetCooldown();
                        makeAiTurns();
            }
        });
    }

    private void makeAiTurns() {
        resetSelection();
        for (final Creep creep : creeps) {
            makeCreepActions(creep);
        }
        playAnimations(false);
        cooldown();
    }

    public void setAiControl(boolean aiControl) {
        this.aiControl = aiControl;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (aiControl) return true;
        else
        return super.dispatchTouchEvent(ev);
    }

    private void makeCreepActions(Creep creep) {
        if (creep.isInRange()) {
            if (creep.attack() == 0) {
                showFinalScore("dead");
            }
        } else {
            creep.moveOneToAttackPosition();
        }
        resetSelection();
    }

    public void showFinalScore(String which) {
        finish();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        String string1 = prefs.getString("scoreboard", "");
        String levels1 = prefs.getString("levelScore", "");
        String names1 = prefs.getString("nameScore", "");
        editor.clear();
        editor.putString("nameScore", names1);
        editor.putString("levelScore", levels1);
        editor.putString("scoreboard", string1);
        Intent intent = new Intent(getApplicationContext(), EndGameScreen.class);
        intent.putExtra("which", which);
        //intent.putExtra("score", player.getScore());//not needed
        String scores = prefs.getString("scoreboard", "");
        String levels = prefs.getString("levelScore", "");
        String names = prefs.getString("nameScore", "");
        Stack<String> list = new Stack<>();
        Stack<String> listLev = new Stack<>();
        Stack<String> listNam = new Stack<>();
        while (scores.contains(";")) {
            list.push(scores.substring(0, scores.indexOf(";")));
            scores = scores.substring(scores.indexOf(";") + 1);
            listLev.push(levels.substring(0, levels.indexOf(";")));
            levels = levels.substring(levels.indexOf(";") + 1);
            listNam.push(names.substring(0, names.indexOf(";")));
            names = names.substring(names.indexOf(";") + 1);
        }
        Stack<String> list1 = new Stack<>();
        Stack<String> listLev1 = new Stack<>();
        Stack<String> listNam1 = new Stack<>();
        while (!list.empty()) {
            list1.push(list.pop());
            listLev1.push(listLev.pop());
            listNam1.push(listNam.pop());
        }
        list1.push(String.valueOf(player.getScore()));
        listLev1.push(String.valueOf(level));
        listNam1.push("DEFAULT");
        String finalScores = "";
        String finalLevels = "";
        String finalNames = "";
        int i = 0;
        while ( !list1.empty() && list1.peek() != null && i < 10) {
            finalScores += list1.pop() + ";";
            finalLevels += listLev1.pop() + ";";
            finalNames += listNam1.pop() + ";";
            i++;
        }
        editor.putString("scoreboard", finalScores);
        editor.putString("levelScore", finalLevels);
        editor.putString("nameScore", finalNames);
        editor.commit();
        startActivity(intent);
    }

    private void nextLevel() {
        currentlySelected = null;
        creeps.clear();
        player.setPillumCount(player.getMaxPillumCount());
        level++;
        player.pickUpPillums();
        player.nullCooldown();
        if (temple.isActivated())
            temple.changeState();
        loadMap(level);
        playAnimations(true);
    }

    private void cooldown() {
        player.shieldCooldown();
        if (player.getCurrentCooldownTime() > 0 && player.getCurrentCooldownTime() != player.getCooldownTime())
            ((Button)findViewById(R.id.shieldBtn)).setText("Cooldown " + player.getCurrentCooldownTime());
        else
            ((Button)findViewById(R.id.shieldBtn)).setText("SHIELD");
    }

    private void highlightPath(List<Tile> path) {
        for (Tile tile : path) {
            if (tile.getEntity() != player) {
                tile.highlightForPath();
            }
        }
    }

    private void highlightRange(List<Tile> tiles) {
        for (Tile tile : tiles) {
            tile.highlightForRange();
        }
    }

    private void resetSelection() {
        for (Tile tile : tiles.values()) {
            tile.setEntity(tile.getEntity());
        }
        currentlySelected = null;
    }

    private void showThrowRange() {
        for (Tile tile : player.getThrowRangeTiles()) {
            tile.highlightForPath();
        }
    }

    private void createBoard() {
        tiles = new HashMap();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        boolean isOdd = true;
        int columnNr = -6;
        int rowNr = 0;
        int i = 0;
        int x = 0, y;
        for (int c = 0; c < nrOfColumns; c++) {
            isOdd = !isOdd;
            if (isOdd)
                rowNr--;
            y = 0;
            for (int r = 0; r < nrOfRows; r++) {
                if (c % 2 == 0 && r == nrOfRows - 1)
                    break;
                Tile tile = new Tile(this, layout);
                tile.setLayoutParams(params);
                tile.setWalkable(true); // set when loading map
                tile.setPosition(columnNr, rowNr + i);
                tile.setDisplayPosition(c, r, isOdd);
                layout.addView(tile);
                tiles.put(tile.getPosition().getTag(), tile);
                String string = tile.getPosition().getTag()+ "  ";
                i++;
                y++;
            }
            x++;
            columnNr++;
            i = 0;
        }
    }

    public void createStaminaImages() {
        for (int i = 0; i < 3; i ++) {
            layout.removeView(layout.findViewWithTag("pillum" + i));
        }
        TextView pillumCount = findViewById(R.id.pillumCount);
        LinearLayout.LayoutParams lLayout =
                new LinearLayout.LayoutParams(layout.getWidth()/9,layout.getWidth()/9);
        for (int i = 0; i < player.getPillumCount(); i++) {
            ImageView pillum = new ImageView(getApplicationContext());
            pillum.setLayoutParams(lLayout);
            pillum.setBackgroundResource(R.drawable.pillum);
            pillum.setX(pillumCount.getX() + layout.getWidth() / 9 - i * layout.getWidth() / 9);
            pillum.setY(pillumCount.getY());
            pillum.requestLayout();
            pillum.setTag("pillum" + i);
            layout.addView(pillum);
        }
        pillumCount.setVisibility(View.INVISIBLE);
    }

    public void createLivesImages() {
        for (int i = 0; i < 3; i ++) {
            layout.removeView(layout.findViewWithTag("heart" + i));
        }
        TextView textHealth = findViewById(R.id.health);
        LinearLayout.LayoutParams lLayout =
                new LinearLayout.LayoutParams(layout.getWidth()/9,layout.getWidth()/9);
        for (int i = 0; i < player.getHealth(); i++) {
            ImageView heart = new ImageView(getApplicationContext());
            heart.setLayoutParams(lLayout);
            heart.setBackgroundResource(R.drawable.health);
            heart.setX(textHealth.getX() - layout.getWidth() / 18 + i * layout.getWidth() / 9);
            heart.setY(textHealth.getY());
            heart.requestLayout();
            heart.setTag("heart" + i);
            layout.addView(heart);
        }
        textHealth.setVisibility(View.INVISIBLE);
        //createStaminaImages();
    }

    private View.OnClickListener findPathListener(final Tile tile) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pos = new Position(0, 0).getTag();
                for (Tile cur : findShortestPath(tile, tiles.get(pos))) {
                    cur.setBackgroundColor(Color.RED);
                }
                pos = null;
            }
        };
        return listener;
    }

    private void loadMap(int levelNr) {
        //reading string
        int id = this.getResources().getIdentifier("level" + String.valueOf(levelNr), "raw", this.getPackageName());

        if (id == 0) {
            showFinalScore("finished");
        }

        InputStream inputStream = getApplicationContext().getResources().openRawResource(id);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String content = text.toString();

        //load walkables from string to array end of line \n space between numbers
        boolean[][] walkables = new boolean[nrOfColumns][nrOfRows];
        //load map into array^2
        String curChar;
        for (int i = 0; i < nrOfRows; i++) {
            for (int j = 0; j < nrOfColumns; j++) {
                if (j % 2 == 0 && i == nrOfRows - 1) {
                    content = content.substring(content.indexOf(" ") + 1 );
                    continue;
                }
                curChar = content.substring(0, content.indexOf(" "));
                content = content.substring(content.indexOf(" ") + 1 );
                walkables[j][i] = curChar.equals("1") ? true : false;
            }
        }
        setWalkablesFromFile(walkables);
        createEntitiesFromFile(content);
    }

    private void createEntitiesFromFile(String content) {
        String curChar;
        String sx,sy;
        int xx,yy;
        while (!content.isEmpty()) {
            curChar = content.substring(0,content.indexOf("|"));
            content = content.substring(content.indexOf("|") + 1 );
            sx = content.substring(0,content.indexOf("|"));
            content = content.substring(content.indexOf("|") + 1 );
            sy = content.substring(0,content.indexOf("|"));
            content = content.substring(content.indexOf(" ") + 1 );
            xx = Integer.parseInt(sx);
            yy = Integer.parseInt(sy);
            switch (curChar) {
                case "P": {
                    player.changePosition(new Position(xx,yy));
                    break;
                }
                case "C": {
                    creeps.add(new Creep(this, new Position(xx,yy)));
                    break;
                }
                case "R": {
                    creeps.add(new RangedCreep(this, new Position(xx,yy)));
                    break;
                }
                case "T": {
                    temple.changePosition(new Position(xx,yy));
                    break;
                }
                case "E": {
                    exit.changePosition(new Position(xx,yy));
                    break;
                }
                case "G": {
                    creeps.add(new Goliath(this, new Position(xx,yy)));
                    break;
                }
                default: {
                    new Throwable("Wrong parameter when reading map entities");
                }
            }
        }
    }

    private void setWalkablesFromFile(boolean[][] walkables) {
        //set walkables
        boolean walkable;
        boolean isOdd = true;
        int columnNr = -6;
        int rowNr = 0;
        int i = 0;
        int x = 0, y;
        for (int c = 0; c < nrOfColumns; c++) {
            isOdd = !isOdd;
            if (isOdd)
                rowNr--;
            y = 0;
            for (int r = 0; r < nrOfRows; r++) {
                if (c % 2 == 0 && r == nrOfRows - 1)
                    break;
                walkable = walkables[c][r];
                Tile tile = tiles.get(new Position(columnNr,rowNr + i).getTag());
                tile.setWalkable(walkable);//c,r from file
                tile.setEntity(null);
                i++;
                y++;
            }
            x++;
            columnNr++;
            i = 0;
        }
    }

    private void createEntities() {
        if (player == null)
           player = new Player(board, new Position(0, 0));
        temple = new Temple(board, new Position(0,0));
        exit = new Exit(board, new Position(0,0));
    }

    public List<Tile> getWalkableNeighbours(Tile center) {
        List<Tile> neighbours = new ArrayList<>();
        for (Tile tile : getSingleRing(center, 1)) {
            if (tile.isWalkable() && (tile.getEntity() == null))
                neighbours.add(tile);
        }
        return neighbours;
    }

    public List<Tile> getNeighbours(Tile center) {
        List<Tile> neighbours = new ArrayList<>();
        for (Tile tile : getSingleRing(center, 1)) {
            neighbours.add(tile);
        }
        return neighbours;
    }

    public List<Tile> getFilledCircle(Tile center, int radius) {
        List<Tile> circle = new ArrayList<>();
        for (int i = 1; i <= radius; i++) {
            circle.addAll(getSingleRing(center, i));
        }
        return circle;
    }

    public List<Tile> getSingleRing(Tile center, int radius) {
        List<Tile> ring = new ArrayList<>();
        if (radius <= 0) return null;
        String position;
        //left
        for (int i = 0; i < radius; i++) {
            position = new Position(center.getPosition().getX() - radius, center.getPosition().getY() + i).getTag();
            if (tiles.containsKey(position))
                ring.add(tiles.get(position));
            //bottom left
            position = new Position(center.getPosition().getX() - radius + i, center.getPosition().getY() + radius).getTag();
            if (tiles.containsKey(position))
                ring.add(tiles.get(position));
            //bottom right
            position = new Position(center.getPosition().getX() + i, center.getPosition().getY() + radius - i).getTag();
            if (tiles.containsKey(position))
                ring.add(tiles.get(position));
            //right
            position = new Position(center.getPosition().getX() + radius, center.getPosition().getY() - i).getTag();
            if (tiles.containsKey(position))
                ring.add(tiles.get(position));
            //top right
            position = new Position(center.getPosition().getX() + radius - i, center.getPosition().getY() - radius).getTag();
            if (tiles.containsKey(position))
                ring.add(tiles.get(position));
            //top left
            position = new Position(center.getPosition().getX() - i, center.getPosition().getY() - radius + i).getTag();
            if (tiles.containsKey(position))
                ring.add(tiles.get(position));
        }
        position = null;
        return ring;
    }

    public List<Tile> findShortestPath(Tile from, Tile to) {
        List<Tile> openSet = new ArrayList<>();
        List<Tile> closedSet = new ArrayList<>();
        openSet.add(from);
        HashMap<Tile, Tile> cameFrom = new HashMap<>();
        HashMap<Tile, Integer> gScore = new HashMap<>();
        HashMap<Tile, Integer> fScore = new HashMap<>();
        for (Tile tile : tiles.values()) {
            gScore.put(tile, 9999);
            fScore.put(tile, 9999);
        }
        gScore.put(from, 0);
        fScore.put(from, estimate(from, to));
        while (!openSet.isEmpty()) {
            Tile current = null;
            //find lowest fScore in openSet
            for (Tile tile : openSet) {
                if (current == null) {
                    current = tile;
                    continue;
                }
                if (fScore.get(current) > fScore.get(tile)) {
                    current = tile;
                }
            }

            if (current == to) {
                return construct_path(cameFrom, current);
            }

            openSet.remove(current);
            closedSet.add(current);

            for (Tile neighbour : getWalkableNeighbours(current)) {
                if (closedSet.contains(neighbour))
                    continue;
                if (!openSet.contains(neighbour))
                    openSet.add(neighbour);
                int testScore = gScore.get(current) + 1; //distance between current and neighbour is 1...for now
                if (testScore >= gScore.get(neighbour))
                    continue;
                cameFrom.put(neighbour, current);
                gScore.put(neighbour, testScore);
                fScore.put(neighbour, gScore.get(neighbour) + estimate(neighbour, to));
            }


        }
        return null;
    }

    private int priceNeighbours(Tile origin, Tile destination) {
        return Math.abs((Math.abs(origin.getPosition().getX()) + Math.abs(origin.getPosition().getY())) -
                (Math.abs(destination.getPosition().getX()) + Math.abs(destination.getPosition().getY())));
    }

    private List<Tile> construct_path(HashMap<Tile, Tile> cameFrom, Tile current) {
        List<Tile> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        return path;
    }

    private Integer estimate(Tile from, Tile to) {
        return Math.abs(from.getPosition().getX() - to.getPosition().getX()) +
                Math.abs(from.getPosition().getY() - to.getPosition().getY());
    }

    public Tile getTileOfDirection(Tile center, Direction direction, int distance) {
        if (distance < 1) {
            throw new IllegalArgumentException("Distance can not be less than 1");
        }
        Tile tile = null;
        switch (direction) {
            case TOP: {
                tile = tiles.get(new Position(center.getPosition().getX(), center.getPosition().getY() - distance).getTag());
                break;
            }
            case BOTTOM: {
                tile = tiles.get(new Position(center.getPosition().getX(), center.getPosition().getY() + distance).getTag());
                break;
            }
            case TOPLEFT: {
                tile = tiles.get(new Position(center.getPosition().getX() - distance, center.getPosition().getY()).getTag());
                break;
            }
            case TOPRIGHT: {
                tile = tiles.get(new Position(center.getPosition().getX() + distance, center.getPosition().getY() - distance).getTag());
                break;
            }
            case BOTTOMLEFT: {
                tile = tiles.get(new Position(center.getPosition().getX() - distance, center.getPosition().getY() + distance).getTag());
                break;
            }
            case BOTTOMRIGHT: {
                tile = tiles.get(new Position(center.getPosition().getX() + distance, center.getPosition().getY()).getTag());
                break;
            }
        }
        return tile;
    }

    public List<Tile> getStar(Tile center, int radius) {
        ArrayList<Tile> star = new ArrayList<>();
        for (int i = 1; i <= radius; i++) {
            for (Direction dir : Direction.values()) {
                Tile tile = getTileOfDirection(center, dir, i);
                if (tile != null)
                    star.add(tile);
            }
        }
        return star;
    }

    public Player getPlayer() {
        return player;
    }

    public HashMap<String, Tile> getTiles() {
        return tiles;
    }

    public ArrayList<Creep> getCreeps() {
        return creeps;
    }

    public void switchView(View view) {
        //Intent backToMainIntent = new Intent();
        //save this
        finish();
        //startActivity(backToMainIntent);
    }

    @Override
    protected void onPause() {
        saveGame();
        super.onPause();
    }

    @Override
    public void increaseHealth(DialogFragment dialog) {
        player.increaseMaxHealth();
    }

    @Override
    public void increasePillumCount(DialogFragment dialog) {
        player.increasePillumCount();
    }

    @Override
    public void restoreHealth(DialogFragment dialog) {
        player.heal();
    }

    @Override
    public void reduceCooldown(DialogFragment dialog) {
        player.reduceCooldown();
    }

    @Override
    public void increaseThrowRange(DialogFragment dialog) {
        player.increaseThrowRange();
    }

    private void loadGame() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        level = prefs.getInt("level",1);
        loadMap(level);
        player.setScore(prefs.getInt("score", 0));
        int i = 0;
        int x,y;
        creeps.clear();
        for (Tile tile : tiles.values()) {
            if (tile.isWalkable() || tile.getEntity() != null)
                tile.getBackground().clearColorFilter();
        }
        while (prefs.contains("Creep" + String.valueOf(i) + "x") || prefs.contains("RangedCreep" + String.valueOf(i) + "x")) {
            if (prefs.contains("Creep" + String.valueOf(i) + "x")) {
                x = prefs.getInt("Creep" + String.valueOf(i) + "x",0);
                y = prefs.getInt("Creep" + String.valueOf(i) + "y",0);
                creeps.add(new Creep(this, new Position(x,y)));
            }
            if (prefs.contains("RangedCreep" + String.valueOf(i) + "x")) {
                x = prefs.getInt("RangedCreep" + String.valueOf(i) + "x",0);
                y = prefs.getInt("RangedCreep" + String.valueOf(i) + "y",0);
                creeps.add(new RangedCreep(this, new Position(x,y)));
            }
            i++;
        }
        x = prefs.getInt("Templex", 0);
        y = prefs.getInt("Templey", 0);
        temple = new Temple(this, new Position(x,y));
        x = prefs.getInt("Exitx", 0);
        y = prefs.getInt("Exity", 0);
        exit = new Exit(this, new Position(x,y));

        //Player
        x = prefs.getInt("Playerx", 0);
        y = prefs.getInt("Playery", 0);
        player = new Player(this, new Position(x,y));
        player.setHealth(prefs.getInt("Playerhealth",0));
        player.setPillumCount(prefs.getInt("Playerstamina",0));
        if (prefs.getBoolean("Health1", false)) {
            player.setMaxLives(player.getMaxLives()+1);
            if (prefs.getBoolean("Health2", false))
                player.setMaxLives(player.getMaxLives()+1);
        }
        if (prefs.getBoolean("Pillum1", false)) {
            player.setMaxPillumCount(player.getMaxPillumCount()+1);
            if (prefs.getBoolean("Pillum2", false))
                player.setMaxPillumCount(player.getMaxPillumCount()+1);
        }
        if (prefs.getBoolean("Shielderino", false))
            player.reduceCooldown();
        if (prefs.getBoolean("Throwerino", false))
            player.setThrowRange(player.getThrowRange()+1);
    }

    private void saveGame() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        String string = prefs.getString("scoreboard", "");
        String levels = prefs.getString("levelScore", "");
        String names = prefs.getString("nameScore", "");
        editor.clear();
        editor.putString("nameScore", names);
        editor.putString("levelScore", levels);
        editor.putString("scoreboard", string);
        editor.putBoolean("hasSave", true);
        int i = 0;
        for (Creep creep : creeps) {
            editor.putInt(creep.getName() + String.valueOf(i) + "x", creep.getPosition().getX());
            editor.putInt(creep.getName() + String.valueOf(i) + "y", creep.getPosition().getY());
            i++;
        }
        editor.putInt(temple.getName() + "x", temple.getPosition().getX());
        editor.putInt(temple.getName() + "y", temple.getPosition().getY());
        editor.putInt(exit.getName() + "x", exit.getPosition().getX());
        editor.putInt(exit.getName() + "y", exit.getPosition().getY());

        //Player
        editor.putInt(player.getName() + "x", player.getPosition().getX());
        editor.putInt(player.getName() + "y", player.getPosition().getY());
        Iterator it = player.getAbilities().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Boolean> pair = (Map.Entry) it.next();
            editor.putBoolean(pair.getKey(), pair.getValue());
            it.remove();
        }
        editor.putInt(player.getName() + "health", player.getHealth());
        editor.putInt(player.getName() + "stamina", player.getPillumCount());

        //Game
        editor.putInt( "score", player.getScore());
        editor.putInt("level", level);

        editor.commit();
    }

    private void createUpgradeMenu() {
        AbilityDialog dialog = new AbilityDialog();
        dialog.setAtributes(getUnlockableAbilitiesList(), getBinaryUnlockables(), temple);
        dialog.show(getFragmentManager(), "Abilities dialog");
    }

    private ArrayList<String> getUnlockableAbilitiesList() {
        ArrayList<String> help = new ArrayList<>();
        HashMap<String, Boolean> abilities = player.getAbilities();
        help.add(getString(R.string.heal));
        if (!abilities.get("Health1") || !abilities.get("Health2")) {
            help.add(getString(R.string.maxHealth));
        }
        if (!abilities.get("Pillum1") || !abilities.get("Pillum2")) {
            help.add(getString(R.string.maxPillum));
        }
        if (!abilities.get("Shielderino")) {
            help.add(getString(R.string.cooldownTime));
        }
        if (!abilities.get("Throwerino")) {
            help.add(getString(R.string.throwRange));
        }
        return help;
    }

    private ArrayList<Boolean> getBinaryUnlockables() {
        ArrayList<Boolean> help = new ArrayList<>();
        HashMap<String, Boolean> abilities = player.getAbilities();
        help.add(true);
        if (!abilities.get("Health1") || !abilities.get("Health2")) {
            help.add(true);
        } else {
            help.add(false);
        }
        if (!abilities.get("Pillum1") || !abilities.get("Pillum2")) {
            help.add(true);
        } else {
            help.add(false);
        }
        if (!abilities.get("Shielderino")) {
            help.add(true);
        } else {
            help.add(false);
        }
        if (!abilities.get("Throwerino")) {
            help.add(true);
        } else {
            help.add(false);
        }
        return help;
    }

    private boolean containsPlayerWithinRange(Entity entity) {
        for (Tile tile : entity.getRange()) {
            if (tile.getEntity() instanceof Player) {
                return true;
            }
        }
        return false;
    }

    public RelativeLayout getLayout() {
        return layout;
    }

    public void addAnimationToList(AnimatorSet animate, Integer dur) {
        int duration = 0;
        for (Integer anim : animationsLengths) {
            duration += anim;
        }
        animate.setStartDelay(duration);
        animations.add(animate);
        animationsLengths.add(dur);
    }

    private void playAnimations(boolean playFast) {
        int duration = 0;
        setAiControl(true);
        for (Integer i : animationsLengths)
            duration += i;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                setAiControl(false);
            }
        },duration, TimeUnit.MILLISECONDS);
        scheduler.shutdown();
        for (AnimatorSet anim : animations) {
            if (playFast) {
                anim.setDuration(0);
                anim.setStartDelay(0);
            }
            anim.start();
        }
        scheduler.shutdown();
        animations.clear();
        animationsLengths.clear();
    }
}
