package soosdev.bakalarka;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by patrik on 1.3.2018.
 */

public class Settings extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        findViewById(R.id.settingsBackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 switchView(view);
            }
        });
    }

    public void switchView(View view) {
        Intent saveSettings = new Intent();
        //send back settings
        //or just save them

        finish();
    }
}
