package soosdev.bakalarka;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main_menu);
        final Button settingsBtn, exitBtn, playBtn, continueBtn, scoreBtn;
        continueBtn = findViewById(R.id.ContBtn);
        scoreBtn = findViewById(R.id.ScoreBtn);
        exitBtn = findViewById(R.id.ExitBtn);
        exitBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                destroy(view);
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (prefs.getBoolean("hasSave", false))
                    switchViewToBoard(view, true);
            }
        });

        playBtn = findViewById(R.id.PlayBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchViewToBoard(view, false);
            }
        });

        settingsBtn = findViewById(R.id.SettingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchViewToSettings(view);
            }
        });

        final Button[] buttons = new Button[]{settingsBtn, exitBtn, playBtn, continueBtn, scoreBtn};

        final ViewTreeObserver vto = findViewById(R.id.mainActivity).getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = findViewById(R.id.mainActivity).getWidth();
                if (width > 0) {
                    findViewById(R.id.mainActivity).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int w = width/3;
                for (Button btn : buttons) {
                    btn.setWidth(w);
                }
            }
        });

        scoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchViewToScore(view);
            }
        });


    }

    public void destroy(View view) {
        Toast.makeText(getApplicationContext(), "Click again to exit", Toast.LENGTH_SHORT).show(); //2s
        final Button button = findViewById(R.id.ExitBtn);
        final boolean[] timeWindowOpen = {true};
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (timeWindowOpen[0]) {
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            }
        });
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                timeWindowOpen[0] = false;
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        destroy(view);
                    }
                });
            }
        },2, TimeUnit.SECONDS);
        scheduler.shutdown();
    }

    private void switchViewToBoard(View view, boolean cont) {
        Intent switchToBoardIntent = new Intent(this, Board.class);
        switchToBoardIntent.putExtra("continue", cont);
        startActivity(switchToBoardIntent);
    }

    private void switchViewToSettings(View view) {
        Intent switchToBoardIntent = new Intent(this, Settings.class);

        startActivity(switchToBoardIntent);
    }

    private void switchViewToScore(View view) {
        Intent switchToScoreIntent = new Intent(this, Scoreboard.class);

        startActivity(switchToScoreIntent);
    }
}
