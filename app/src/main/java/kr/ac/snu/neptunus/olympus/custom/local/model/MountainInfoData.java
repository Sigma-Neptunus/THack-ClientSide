package kr.ac.snu.neptunus.olympus.custom.local.model;

import com.skp.Tmap.TMapPoint;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jjc93 on 2016-03-03.
 */
public class MountainInfoData implements Serializable {
    private static String TAG = MountainInfoData.class.getName();

    private String thumbnailUrl;
    private String name;
    private String location;
    private double height;
    private List<TMapPoint> points;

    public MountainInfoData(String t, String n, String l, double h, List<TMapPoint> ps) {
        thumbnailUrl = t;
        name = n;
        location = l;
        height = h;
        points = ps;
    }

/*    @Override
    public String toString() {
        return "Mountain " + name + ";\n" + "height=" + height + "location=" + location;
    }*/

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getHeight() {
        return height;
    }

    public List<TMapPoint> getPoints() {
        return points;
    }
}
