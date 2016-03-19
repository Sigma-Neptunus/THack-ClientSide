package kr.ac.snu.neptunus.olympus.custom.local.model;

/**
 * Created by jjc93 on 2016-03-19.
 */
public class MountainHistoryData {
    private static final String TAG = MountainHistoryData.class.getName();

    private String name;
    private int step;
    private int heartBeat;
    private String date;
    private String time;

    public MountainHistoryData(String n, int s, int h, String d, String t) {
        name = n;
        step = s;
        heartBeat = h;
        date = d;
        time = t;
    }

    public String getName() {
        return name;
    }

    public int getStep() {
        return step;
    }

    public float getHeartBeat() {
        return heartBeat;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
