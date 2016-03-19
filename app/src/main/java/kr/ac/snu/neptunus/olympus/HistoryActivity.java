package kr.ac.snu.neptunus.olympus;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HistoryActivity extends AppCompatActivity {

    Toolbar toolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initToolbar();
        initProgressbar();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle("");
        TextView titleView = (TextView) toolbar.findViewById(R.id.action_bar_title);
        titleView.setText("기록");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initProgressbar() {
        Drawable drawable = getResources().getDrawable(R.drawable.progress);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setProgressDrawable(drawable);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
