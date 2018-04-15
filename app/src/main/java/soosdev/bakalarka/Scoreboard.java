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

public class Scoreboard extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scoreboard);
        ((TextView)findViewById(R.id.scoreBoardTextView)).setTextSize(25);
        ((TextView)findViewById(R.id.scoreBoardLevels)).setTextSize(25);
        ((TextView)findViewById(R.id.scoreBoardNames)).setTextSize(25);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String nameScore = prefs.getString("nameScore", "");
        String playerName = getIntent().getStringExtra("playerName");
        if (playerName != null )
            nameScore = nameScore.replace("DEFAULT", playerName);
        prefs.edit().putString("nameScore", nameScore);
        prefs.edit().commit();
        ((TextView)findViewById(R.id.scoreBoardTextView)).setText(prefs.getString("scoreboard", "").replaceAll(";", "\n"));
        ((TextView)findViewById(R.id.scoreBoardLevels)).setText(prefs.getString("levelScore", "").replaceAll(";", " ->\n"));
        ((TextView)findViewById(R.id.scoreBoardNames)).setText(nameScore.replaceAll(";", " ->\n"));
        findViewById(R.id.mainMenuBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
