package soosdev.bakalarka;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by patrik on 22.3.2018.
 */

public class EndGameScreen extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_game);

        String screen = getIntent().getStringExtra("which");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String str = prefs.getString("scoreboard", "");
        String level = prefs.getString("levelScore", "");
        str = prefs.getString("scoreboard", "").substring(0, str.indexOf(";"));
        level = prefs.getString("levelScore", "").substring(0, level.indexOf(";"));

        String text;
        switch (screen) {
            case "dead": {
                text = "You died!";
                break;
            }
            case "finished": {
                text = "You won!";
                break;
            }
            case "no_turn": {
                text = "You got stuck!";
                break;
            }
            default: text = "EndGameScreen switch default";
        }

        TextView screenName = (TextView) findViewById(R.id.endGameYoudied);
        TextView scoreStr = (TextView) findViewById(R.id.endGameYourScore);
        screenName.setTextSize(35);
        screenName.setText(text);
        scoreStr.setTextSize(20 );
        scoreStr.setText(level + " -> " + str);


        findViewById(R.id.scoreBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = ((EditText)findViewById(R.id.endGameEditText)).getText().toString();
                if (string.equals(R.string.entername) || string.equals("")) {
                    (findViewById(R.id.endGameEditText)).setBackgroundResource(R.drawable.edittextborder);
                } else {
                    Intent intent = new Intent(getApplicationContext(), Scoreboard.class);
                    intent.putExtra("playerName", string);
                    startActivity(intent);
                }
            }
        });
    }
}
