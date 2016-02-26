package kr.ac.snu.neptunus_stick.stick;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.skp.Tmap.TMapView;

public class MapActivity extends AppCompatActivity {

    TMapView tMapView = null;
    Toolbar toolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initToolbar();
        initTMap();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("Stick App");
        setSupportActionBar(toolbar);
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
}
