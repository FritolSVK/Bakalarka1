package soosdev.bakalarka;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by patrik on 2.3.2018.
 */

public class HexagonalButton extends AppCompatImageButton {
    public HexagonalButton(Context context) {
        super(context);

    }

    public OnTouchListener createOnClickListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (((AppCompatImageButton)view).getDrawable().getBounds()
                            .contains(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()))) {
                        doAction();
                    }
                }
                return false;
            }
        };
    }

    public void setBackgroundResource(int id, RelativeLayout layout) {
        super.setBackgroundResource(id);
        this.getLayoutParams().height =  layout.getWidth()/9;
        this.getLayoutParams().width =  layout.getWidth()/9;
    }

    private void doAction() {

    }
}
