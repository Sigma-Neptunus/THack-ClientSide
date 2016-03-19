package kr.ac.snu.neptunus.olympus.custom.local.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import kr.ac.snu.neptunus.olympus.custom.local.model.MountainHistoryData;

/**
 * Created by jjc93 on 2016-03-19.
 */
public class MountainHistoryAdapter extends BaseAdapter {
    private static final String TAG = MountainHistoryAdapter.class.getName();

    private Context context = null;
    private List<MountainHistoryData> source = null;

    public MountainHistoryAdapter(Context context, List<MountainHistoryData> source) {
        this.context = context;
        this.source = source;
    }
    @Override
    public int getCount() {
        return source.size();
    }

    @Override
    public Object getItem(int position) {
        return source.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        return null;
    }
}
