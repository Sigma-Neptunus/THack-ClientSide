package kr.ac.snu.neptunus.olympus;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.skp.Tmap.TMapView;

public class MapActivity extends AppCompatActivity {
    private static String TAG = MapActivity.class.getName();

    private TMapView tMapView = null;
    private Toolbar toolbar = null;
    private LinearLayout bottomBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initToolbar();
        initTMap();
        initBottomBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings :
                Intent intent0 = new Intent(this, SettingActivity.class);
                startActivity(intent0);
                return true;
            case R.id.history :
                Intent intent1 = new Intent(this, HistoryActivity.class);
                startActivity(intent1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        toolbar.inflateMenu(R.menu.menu_map);
        toolbar.setTitle("지도");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initTMap() {
        int TMapLanguage = TMapView.LANGUAGE_KOREAN;

        tMapView = (TMapView) findViewById(R.id.tmap);
        tMapView.setSKPMapApiKey(getString(R.string.tmap_apikey));
        tMapView.setLanguage(TMapLanguage);
        tMapView.setIconVisibility(true);
        tMapView.setZoomLevel(10);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setCompassMode(true);
        tMapView.setTrackingMode(true);
    }

    private void initBottomBar() {
        bottomBar = (LinearLayout) findViewById(R.id.bottom_bar);
        bottomBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }

            private void expand(final View v) {
                v.setVisibility(View.VISIBLE);

                final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                v.measure(widthSpec, heightSpec);

                ValueAnimator animator = slideAnimator(0, v.getMeasuredHeight(), v);
                animator.start();
            }

            private void collapse(final View v) {
                int currentHeight = v.getHeight();

                ValueAnimator animator = slideAnimator(currentHeight, 0, v);

                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                animator.start();
            }

            private ValueAnimator slideAnimator(int start, int end, final View v) {
                final ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int height = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                        layoutParams.height = height;
                        v.setLayoutParams(layoutParams);
                    }
                });
                return valueAnimator;
            }
        });
    }
}
