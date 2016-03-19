package kr.ac.snu.neptunus.olympus;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.skp.Tmap.TMapPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.ac.snu.neptunus.olympus.custom.local.controller.MountainInfoAdapter;
import kr.ac.snu.neptunus.olympus.custom.local.model.MountainInfoData;

public class SearchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static String TAG = SearchActivity.class.getName();
    private static boolean isSplashed = false;

    private Toolbar toolbar = null;
    private SearchView searchView = null;
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (!isSplashed) {
            SplashActivity.splash(this);
            isSplashed = true;
        }
        initToolbar();
        initSearchView();
        initListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history :
                Intent intent1 = new Intent(this, HistoryActivity.class);
                startActivity(intent1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
    }

    private ArrayList<MountainInfoData> datas = null;
    private MountainInfoAdapter adapter = null;

    private void initSearchView() {
        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                Log.d(TAG, newText);
                return true;
            }
        });
    }

    private void initListView() {
        datas = new ArrayList<>();
        initDummies();
        adapter = new MountainInfoAdapter(this, datas);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchActivity.this, MapActivity.class);
                MountainInfoData mountainInfoData = (MountainInfoData) parent.getAdapter().getItem(position);
                MapActivity.setMountainInfoData(mountainInfoData);
                startActivity(intent);
            }
        });
    }

    private void initDummies() {
        MountainInfoData dummy = getInfoData(R.raw.gwanak);
        datas.add(dummy);
        dummy = getInfoData(R.raw.bukhan);
        datas.add(dummy);
        dummy = getInfoData(R.raw.gwanggyo);
        datas.add(dummy);
    }

    private MountainInfoData getInfoData(int id) {
        ArrayList<TMapPoint> pointArray = new ArrayList<>();

        String rawdata = null;
        InputStream inputStream = getResources().openRawResource(id);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int c;
        try {
            int length = inputStream.available();
            c = inputStream.read();
            while (c != -1) {
                byteArrayOutputStream.write(c);
                c = inputStream.read();
            }

            rawdata = byteArrayOutputStream.toString();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, rawdata);
        String middle = rawdata.replaceAll("\\r\\n|(\\n|\\r)","\r\n");
        String[] points = middle.split("\\r\\n");
        Pattern pattern = Pattern.compile("(\\d*\\.\\d*), (\\d*\\.\\d*).", Pattern.DOTALL);
        Matcher matcher = null;
        String thumbnail = points[0];
        String name = points[1];
        String location = points[2];
        String height = points[3];
        for (int i = 4; i < points.length; i++) {
            matcher = pattern.matcher(points[i]);
            if (matcher.matches()) {
                pointArray.add(new TMapPoint(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2))));
            }
        }
        return new MountainInfoData(thumbnail, name, location, Double.valueOf(height), (List) pointArray);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }
}
