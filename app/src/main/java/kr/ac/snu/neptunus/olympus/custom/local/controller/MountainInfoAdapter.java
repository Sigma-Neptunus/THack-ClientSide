package kr.ac.snu.neptunus.olympus.custom.local.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import kr.ac.snu.neptunus.olympus.R;
import kr.ac.snu.neptunus.olympus.custom.local.model.MountainInfoData;
import kr.ac.snu.neptunus.olympus.custom.view.GenericViewHolder;

/**
 * Created by jjc93 on 2016-03-03.
 */
public class MountainInfoAdapter extends BaseAdapter {
    private static final String TAG = MountainInfoAdapter.class.getName();

    private List<MountainInfoData> dataList = null;
    private ArrayList<MountainInfoData> searchedDataList = null;
    private Context context = null;

    public MountainInfoAdapter(Context context, List<MountainInfoData> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.searchedDataList = new ArrayList<>(dataList);
        notifyDataSetChanged();
    }

    public void setDataList(List<MountainInfoData> dataList) {
        this.dataList = dataList;
        this.searchedDataList = new ArrayList<>(dataList);
        notifyDataSetChanged();
    }

    public void filter(String name) {
        if (!name.isEmpty()) {
            this.searchedDataList = new ArrayList<>();
            for (MountainInfoData m : dataList) {
                if (SoundSearcher.matchString(m.getName(), name)) {
                    searchedDataList.add(m);
                }
            }
        } else {
            this.searchedDataList = new ArrayList<>(dataList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return searchedDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchedDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = null;
        if (context != null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.layout_mountain_info, parent, false);
            }

            MountainInfoData data = (MountainInfoData) getItem(position);

            ImageView thumbnail = GenericViewHolder.get(convertView, R.id.thumbnail);
            TextView name = GenericViewHolder.get(convertView, R.id.name);
            TextView height = GenericViewHolder.get(convertView, R.id.height);
            TextView location = GenericViewHolder.get(convertView, R.id.location);

            Glide.with(context).load(data.getThumbnailUrl()).into(thumbnail);
            name.setText(data.getName());
            height.setText(Double.toString(data.getHeight()) + "m");
            location.setText(data.getLocation());

            return convertView;
        } else {
            return null;
        }
    }
}
