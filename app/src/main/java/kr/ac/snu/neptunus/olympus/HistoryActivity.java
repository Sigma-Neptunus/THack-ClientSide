package kr.ac.snu.neptunus.olympus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class HistoryActivity extends AppCompatActivity {

    Toolbar toolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle("기록");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
