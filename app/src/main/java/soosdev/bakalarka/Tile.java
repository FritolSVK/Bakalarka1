package soosdev.bakalarka;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.Shape;
import android.support.v7.widget.AppCompatButton;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import soosdev.bakalarka.Entities.Creep;
import soosdev.bakalarka.Entities.Entity;
import soosdev.bakalarka.Entities.Exit;
import soosdev.bakalarka.Entities.Player;
import soosdev.bakalarka.Entities.RangedCreep;
import soosdev.bakalarka.Entities.Temple;

/**
 * Created by patrik on 2.3.2018.
 */

public class Tile extends HexagonalButton {//will extend hexagon button
    private RelativeLayout layout;
    private Position position;
    private boolean walkable = false;
    private Entity entity;

    public Tile(Context context, RelativeLayout layout) {
        super(context);
        this.layout = layout;
    }

    public void setPosition(int columnNr, int rowNr) {
        position = new Position(columnNr,rowNr);
        this.setTag(position.getTag());
        //this.setText(position.toString());
    }

    public void setDisplayPosition(int column,final  int row, boolean isOdd) {
        this.setLayoutParams(new RelativeLayout.LayoutParams(layout.getWidth()/9,layout.getWidth()/9));
        final int viewWidth = layout.getWidth();
        final int viewHeight = layout.getHeight();
        final int posX = (viewWidth/12 * column) + 25 ;
        float posY;
        if (isOdd) {
            posY = (float) (viewHeight / 5 + (viewWidth / 14 * row * 1.4) + 25);
            this.setX(posX);
            this.setY(posY);
        }
        else {
            final Tile dis = this;
            ViewTreeObserver vto = layout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width  = layout.getMeasuredWidth();
                    int height = layout.getMeasuredHeight();
                    if (width > 0 && height > 0)
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    float posY = (float) (viewHeight / 5 + (viewWidth / 14 * row * 1.4) + dis.getHeight() / 1.555555);
                    dis.setX(posX);
                    dis.setY(posY);
                }
            });
        }
    }

    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public void setEntity(Entity entity) {
            this.entity = entity;
        if (entity == null) {
            setBackground(entity);
        }
    }

    public void setBackground(Entity entity) {
        this.setImageResource(0);
        this.setBackgroundResource(0);
        if (entity == null) {
            this.setBackgroundResource(R.drawable.tile, layout);
        }
        if (!walkable && entity == null) {
            this.setBackgroundResource(R.drawable.unwalkable, layout);
        }
        if (entity != null) {
            this.setBackgroundResource(R.drawable.tile, layout);
            this.setImageResource(entity.getImageResource());
            this.setScaleType(ScaleType.FIT_CENTER);
        }
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Position getPosition() {
        return position;
    }

    public void highlightForRange() {
        this.setBackgroundResource(R.drawable.tilerangehighlight);
    }

    public void highlightForPath() {
        this.setBackgroundResource(R.drawable.tilepathhighlight);
    }
}
