package kr.ac.snu.neptunus.olympus;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skp.Tmap.TMapCircle;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import org.apache.http.conn.scheme.HostNameResolver;
import org.json.JSONException;
import org.json.JSONObject;

import kr.ac.snu.neptunus.olympus.custom.local.model.MountainInfoData;
import kr.ac.snu.neptunus.olympus.custom.network.controller.SocketNetwork;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapActivity extends AppCompatActivity implements LocationListener {
    private static String TAG = MapActivity.class.getName();

    private static MountainInfoData mountainInfoData = null;

    public static void setMountainInfoData(MountainInfoData mountainInfoData) {
        MapActivity.mountainInfoData = mountainInfoData;
    }

    private Toolbar toolbar = null;

    private RelativeLayout relativeLayout = null;
    private TMapView tMapView = null;
    private double GPSAccuracy = 0;

    private TextView stepView = null;
    private int step = 0;
    private SensorManager sensorManager = null;
    private Sensor stepSensor = null;

    private TextView rateView = null;

    private Chronometer timeView = null;

    private LinearLayout bottomBar = null;

    private int pointLevel = 0;


    private SocketNetwork network = null;

    private Runnable rep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (mountainInfoData == null) {
            Toast.makeText(this, "에러가 발생했습니다.\n계속해서 에러가 발생할 경우 알려주세요.", Toast.LENGTH_SHORT).show();
            finish();
        }

        initConnection();

        initToolbar();
        initTMap();
        checkGps(true);
        initStepCounter();
        initHeartRate();
        initTimer();
        initBottomBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                showBackAlert();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showBackAlert();
    }

    private void showBackAlert() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        View alertView = getLayoutInflater().inflate(R.layout.alertdialog_back, null);

        alertDialogBuilder.setView(alertView);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button posButton = (Button) alertView.findViewById(R.id.pos_button);
        posButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapActivity.super.onBackPressed();
            }
        });
        Button negButton = (Button) alertView.findViewById(R.id.neg_button);
        negButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        checkGps(false);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle("");
        TextView titleView = (TextView) toolbar.findViewById(R.id.action_bar_title);
        titleView.setText(mountainInfoData.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initTMap() {
        int TMapLanguage = TMapView.LANGUAGE_KOREAN;

        relativeLayout = (RelativeLayout) findViewById(R.id.tmap);
        tMapView = new TMapView(this);
        //tMapView.setLocationPoint(126.952321, 37.448961);
        tMapView.setSKPMapApiKey(getString(R.string.tmap_apikey));
        tMapView.setLanguage(TMapLanguage);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);

        initTMapPath();

        relativeLayout.addView(tMapView);
    }

    private void initTMapPath() {
        TMapPolyLine tMapPolyLine = new TMapPolyLine();
        for (int i = 0; i < mountainInfoData.getPoints().size(); i++) {
            tMapPolyLine.addLinePoint(mountainInfoData.getPoints().get(i));
        }
        tMapPolyLine.setLineColor(0xff0000);
        tMapPolyLine.setLineAlpha(127);
        tMapPolyLine.setLineWidth(20);
        tMapView.addTMapPath(tMapPolyLine);
        tMapView.showFullPath(tMapPolyLine);
        tMapView.MapZoomOut();
    }

    private LocationManager locationManager = null;

    private void checkGps(boolean alert) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.d(TAG, "GP=" + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) + ",NET=" + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (alert || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                showGpsAlert();
            } else {
                initGps(false);
            }
        } else {
            initGps(true);
        }
    }

    private void initGps(boolean isGps) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(isGps ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER, 100, 0.05f, this);
        onLocationChanged(locationManager.getLastKnownLocation(isGps ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER));
    }

    private void showGpsAlert() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        View alertView = getLayoutInflater().inflate(R.layout.alertdialog_gps, null);

        alertDialogBuilder.setView(alertView);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button posButton = (Button) alertView.findViewById(R.id.pos_button);
        posButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MapActivity.this.startActivityForResult(intent, 0);
                alertDialog.dismiss();
            }
        });
        Button negButton = (Button) alertView.findViewById(R.id.neg_button);
        negButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                checkGps(false);
            }
        });
    }

    private long lastTime, currentTime, gapTime;
    private float lastX, lastY, lastZ;
    private float x, y, z;
    private float speed;

    private void initStepCounter() {
        stepView = (TextView) findViewById(R.id.step_view);
        stepView.setText(String.format("%d",step));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    currentTime = System.currentTimeMillis();
                    gapTime = currentTime - lastTime;
                    if (gapTime > 800) {
                        lastTime = currentTime;
                        x = Math.abs(event.values[SensorManager.DATA_X]);
                        y = Math.abs(event.values[SensorManager.DATA_Y]);
                        z = Math.abs(event.values[SensorManager.DATA_Z]);

                        speed = (float) Math.sqrt((double) ((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ))) / 800;

                        if (speed > 0.005f) {
                            step++;
                            stepView.setText(String.format("%d", step));
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("step", step);
                                network.sendData(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        lastX = x;
                        lastY = y;
                        lastZ = z;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, stepSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void initHeartRate() {
        rateView = (TextView) findViewById(R.id.rate_view);
    }

    private void initTimer() {
        timeView = (Chronometer) findViewById(R.id.time_view);
        timeView.setText("00:00:00");
        timeView.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                CharSequence text = chronometer.getText();
                if (text.length() == 5) {
                    chronometer.setText("00:"+text);
                } else if (text.length() == 7) {
                    chronometer.setText("0"+text);
                }
            }
        });
        timeView.start();
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

    private void initConnection() {
        Log.d(TAG, "Connections");
        network = new SocketNetwork();
        network.defaultSettings();
        network.setOnDataListener(new SocketNetwork.OnDataListener() {
            @Override
            public void useData(final JSONObject jsonObject) {
                Log.d(TAG, jsonObject.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rateView != null) {
                            rateView.setText(String.format("%d", jsonObject.optInt("pulse")));
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d(TAG, location.toString());
            tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
            TMapCircle tMapCircle = new TMapCircle();
            tMapCircle.setCenterPoint(new TMapPoint(location.getLatitude(), location.getLongitude()));
            Log.d(TAG, "" + location.getAccuracy());
            tMapCircle.setRadius(location.getAccuracy());
            tMapCircle.setAreaColor(0x550000ff);
            tMapCircle.setAreaAlpha(127);
            tMapView.addTMapCircle("acc", tMapCircle);
            TMapPoint target = null;

            int closestIndex = closestPoint(mountainInfoData.getPoints().toArray(new TMapPoint[mountainInfoData.getPoints().size()]), location);
            if (closestIndex == pointLevel) {
                if (distance(location, mountainInfoData.getPoints().get(closestIndex)) < 0.01) {
                    pointLevel++;
                    Log.d(TAG, "pL=" + pointLevel);
                }
            }
            target = mountainInfoData.getPoints().get(pointLevel);
            sendJSON(location.getLatitude(), location.getLongitude(), target.getLatitude(), target.getLongitude());
        } else {
            TMapPoint target = null;
            target = mountainInfoData.getPoints().get(pointLevel);
            sendJSON(tMapView.getLocationPoint().getLatitude(), tMapView.getLocationPoint().getLongitude(), target.getLatitude(), target.getLongitude());
        }
        tMapView.forceLayout();
        relativeLayout.forceLayout();
    }

    private void sendJSON(double latc, double lonc, double latt, double lont){
        try {
            JSONObject jsonObject = new JSONObject("{\ncurrent:{\nlongitude:"+lonc+",\nlatitude:"+latc+"\n},\nto:{\nlongitude:"+lont+",\nlatitude:"+latt+"\n}\n}");
            Log.d(TAG, jsonObject.toString());
            if (network != null) {
                network.sendData(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int closestPoint(TMapPoint[] points, Location current) {
        int minIndex = 0;

        for (int i = 0; i < points.length; i++) {
            if (distance(current, points[minIndex]) > distance(current, points[i])) {
                minIndex = i;
            }
        }
        return minIndex;
    }
    private double closestDistance(TMapPoint[] points, Location current) {
        double minDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < points.length - 1; i++) {
            minDistance = Math.min(minDistance, distance(points[i], points[i+1], new TMapPoint(current.getLatitude(), current.getLongitude())));
        }

        return minDistance;
    }
    private double distance(Location location, TMapPoint tMapPoint) {
        return distance(new TMapPoint(location.getLatitude(), location.getLongitude()), tMapPoint);
    }
    private double distance(TMapPoint tMapPoint1, TMapPoint tMapPoint2) {
        double lon1 = tMapPoint1.getLongitude();
        double lat1 = tMapPoint1.getLatitude();
        double lon2 = tMapPoint2.getLongitude();
        double lat2 = tMapPoint2.getLatitude();
        double rad = 6371;

        double a12 = Math.pow(Math.sin((lat1-lat2)/2), 2)+Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin((lon1-lon2)/2), 2);
        double d12 = 2 * rad * Math.atan2(Math.sqrt(a12), Math.sqrt(1-a12));
        return d12;
    }
    private double distance(TMapPoint line1, TMapPoint line2, TMapPoint tMapPoint) {
        double lat1 = Math.toRadians(line1.getLatitude());
        double lon1 = Math.toRadians(line1.getLongitude());
        double lat2 = Math.toRadians(line2.getLatitude());
        double lon2 = Math.toRadians(line2.getLongitude());
        double lat3 = Math.toRadians(tMapPoint.getLatitude());
        double lon3 = Math.toRadians(tMapPoint.getLongitude());
        double rad = 6371;

        double y13 = Math.sin(lat1 - lat3)*Math.cos(lon3);
        double x13 = Math.cos(lon1)*Math.sin(lon3) - Math.sin(lon1)*Math.cos(lon3)*Math.cos(lat1-lat3);
        double b13 = Math.atan2(y13, x13);

        double y12 = Math.sin(lat1 - lat2)*Math.cos(lon2);
        double x12 = Math.cos(lon1)*Math.sin(lon2) - Math.sin(lon1)*Math.cos(lon2)*Math.cos(lat1-lat2);
        double b12 = Math.atan2(y12, x12);

        double a13 = Math.pow(Math.sin((lat1-lat3)/2),2)+Math.cos(lat1)*Math.cos(lat3)*Math.pow(Math.sin((lon1-lon3)/2), 2);
        double d13 = 2 * rad * Math.atan2(Math.sqrt(a13), Math.sqrt(1-a13));

        Log.d(TAG, "d13 : " + d13);

        double dxt = Math.abs(Math.asin(Math.sin(d13/rad)*Math.sin(b13-b12))*rad);
        Log.d(TAG, "cds : " + dxt);
        return dxt;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, provider + " : " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, provider);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}