package kr.ac.snu.neptunus.olympus;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import kr.ac.snu.neptunus.olympus.custom.local.controller.MountainInfoAdapter;
import kr.ac.snu.neptunus.olympus.custom.local.model.MountainInfoData;

public class SearchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static String TAG = SearchActivity.class.getName();
    private static boolean isSplashed = false;

    private Toolbar toolbar = null;
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
        setSupportActionBar(toolbar);
    }

    private ArrayList<MountainInfoData> datas = null;

    private void initListView() {
        datas = new ArrayList<>();
        initDummies();

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new MountainInfoAdapter(this, datas));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchActivity.this, MapActivity.class);
                MountainInfoData mountainInfoData = (MountainInfoData) parent.getAdapter().getItem(position);
                intent.putExtra("a", mountainInfoData);
                startActivity(intent);
            }
        });
    }

    private void initDummies() {
        MountainInfoData dummy = new MountainInfoData("", "가짜1", "이 지구 어딘가", 1000.0, (List) new ArrayList<>());
        datas.add(dummy);
        dummy = new MountainInfoData("", "가짜2", "서울 어딘가", 523.4, (List) new ArrayList<>());
        datas.add(dummy);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }
}
