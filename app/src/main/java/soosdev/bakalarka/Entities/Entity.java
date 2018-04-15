package soosdev.bakalarka.Entities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import soosdev.bakalarka.Board;
import soosdev.bakalarka.Position;
import soosdev.bakalarka.R;
import soosdev.bakalarka.Tile;

/**
 * Created by patrik on 4.3.2018.
 */

public class Entity {
    protected Position position;
    final protected Board board;

    public Entity(Board board, Position position) {
        this.position = position;
        this.board = board;
        board.getTiles().get(position.getTag()).setEntity(this);
    }

    public void changePosition(Position position) {
        board.getTiles().get(this.position.getTag()).setEntity(null);
        board.addAnimationToList(animateChangePosition(this.position, position),200);
        this.position.set(position);
        board.getTiles().get(this.position.getTag()).setEntity(this);
    }

    protected AnimatorSet animateChangePosition(Position from, Position to) {
        final ImageView imageView = new ImageView(board.getApplicationContext());
        imageView.setImageResource(getImageResource());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(board.getLayout().getWidth()/18,board.getLayout().getWidth()/18));
        imageView.setX(board.getTiles().get(from.getTag()).getX() + board.getLayout().getWidth()/36);
        imageView.setY(board.getTiles().get(from.getTag()).getY() + board.getLayout().getWidth()/36);
        board.getLayout().addView(imageView);


        AnimatorSet animSetXY = new AnimatorSet();

        ObjectAnimator y = ObjectAnimator.ofFloat(imageView,
                "translationY",imageView.getY(), board.getTiles().get(to.getTag()).getY() + board.getLayout().getWidth()/36);

        ObjectAnimator x = ObjectAnimator.ofFloat(imageView,
                "translationX", imageView.getX(), board.getTiles().get(to.getTag()).getX() + board.getLayout().getWidth()/36);

        animSetXY.playTogether(x, y);
        animSetXY.setInterpolator(new LinearInterpolator());
        animSetXY.setDuration(200);
        final Entity entity = this;
        animSetXY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                board.getTiles().get(position.getTag()).setBackground(entity);
                board.getLayout().removeView(imageView);
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        return animSetXY;
    }

    protected AnimatorSet animateAttackMelee(final Position from, final Position to) {
        final ImageView imageView = new ImageView(board.getApplicationContext());
        imageView.setImageResource(getImageResource());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(board.getLayout().getWidth()/18,board.getLayout().getWidth()/18));
        imageView.setX(board.getTiles().get(from.getTag()).getX() + board.getLayout().getWidth()/36);
        imageView.setY(board.getTiles().get(from.getTag()).getY() + board.getLayout().getWidth()/36);
        board.getLayout().addView(imageView);


        AnimatorSet animateThere = new AnimatorSet();

        ObjectAnimator y = ObjectAnimator.ofFloat(imageView,
                "translationY",imageView.getY(), board.getTiles().get(to.getTag()).getY() + board.getLayout().getWidth()/36);

        ObjectAnimator x = ObjectAnimator.ofFloat(imageView,
                "translationX", imageView.getX(), board.getTiles().get(to.getTag()).getX() + board.getLayout().getWidth()/36);

        animateThere.playTogether(x, y);
        animateThere.setInterpolator(new LinearInterpolator());
        animateThere.setDuration(100);

        final AnimatorSet animateBack = new AnimatorSet();

        ObjectAnimator yy = ObjectAnimator.ofFloat(imageView,
                "translationY", board.getTiles().get(to.getTag()).getY() + board.getLayout().getWidth()/36,imageView.getY());

        ObjectAnimator xx = ObjectAnimator.ofFloat(imageView,
                "translationX", board.getTiles().get(to.getTag()).getX() + board.getLayout().getWidth()/36, imageView.getX());

        animateBack.playTogether(xx, yy);
        animateBack.setInterpolator(new LinearInterpolator());
        animateBack.setDuration(200);
        final Entity entity = this;
        final Entity enemy = board.getTiles().get(to.getTag()).getEntity();
        animateThere.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if ((enemy instanceof Creep && !(enemy instanceof Goliath))
                        || (enemy instanceof Goliath && ((Goliath)enemy).wasHit()))
                board.getTiles().get(to.getTag()).setBackground((Entity)null);
                animateBack.start();
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                board.getTiles().get(from.getTag()).setBackground((Entity)null);
                board.getTiles().get(to.getTag()).setBackground(enemy);
                super.onAnimationStart(animation);
            }
        });

        animateBack.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                board.getLayout().removeView(imageView);
                board.getTiles().get(from.getTag()).setBackground(entity);
                super.onAnimationEnd(animation);
            }

        });
        return animateThere;
    }

    protected AnimatorSet animateRangedAttack(final Position from, final Position to) {
        final ImageView imageView = new ImageView(board.getApplicationContext());
        imageView.setImageResource(getImageResource());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(board.getLayout().getWidth()/18,board.getLayout().getWidth()/18));
        imageView.setX(board.getTiles().get(from.getTag()).getX() + board.getLayout().getWidth()/36);
        imageView.setY(board.getTiles().get(from.getTag()).getY() + board.getLayout().getWidth()/36);
        board.getLayout().addView(imageView);


        AnimatorSet animSetXY = new AnimatorSet();

        ObjectAnimator y = ObjectAnimator.ofFloat(imageView,
                "translationY",imageView.getY(), board.getTiles().get(to.getTag()).getY() + board.getLayout().getWidth()/36);

        ObjectAnimator x = ObjectAnimator.ofFloat(imageView,
                "translationX", imageView.getX(), board.getTiles().get(to.getTag()).getX() + board.getLayout().getWidth()/36);

        animSetXY.playTogether(x, y);
        animSetXY.setInterpolator(new LinearInterpolator());
        animSetXY.setDuration(200);
        final Entity entity = this;
        final Entity enemy = board.getTiles().get(to.getTag()).getEntity();
        animSetXY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (enemy instanceof Creep)
                    board.getTiles().get(to.getTag()).setBackground((Entity)null);
                board.getLayout().removeView(imageView);
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (enemy instanceof Creep) {
                    board.getTiles().get(to.getTag()).setBackground(enemy);
                }
                super.onAnimationStart(animation);
            }
        });
        return animSetXY;
    }

    public Position getPosition() {
        return position;
    }

    public List<Tile> getRange() {
        return null;
    }

    public int moveOneToPoint(Tile finalDestination) {
        if (board.getWalkableNeighbours(board.getTiles().get(position.getTag())).size() == 0) {
            return -1; //has no walkable neighbours
        }
        List<Tile> path = board.findShortestPath(board.getTiles().get(position.getTag()), finalDestination);
        if (path == null) {
            return -2; //path doesnt exist
        }
        Tile whereTo = path.get(path.size() - 2);
        changePosition(whereTo.getPosition());
        return 0; //entity moved
    }

    public int moveOneByPath(List<Tile> path) {
        if (path == null)
            return -2;
        Tile whereTo = path.get(path.size() - 2);
        changePosition(whereTo.getPosition());
        return 0; //moved
    }

    public Tile getOccupiedTile() {
        return board.getTiles().get(position.getTag());
    }

    public String getName() {
        return "Entity";
    }

    public int getImageResource() {
        return 0;
    }

}
