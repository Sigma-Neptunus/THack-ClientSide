package kr.ac.snu.neptunus.olympus;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import org.json.JSONObject;

import kr.ac.snu.neptunus.olympus.custom.local.model.MountainInfoData;
import kr.ac.snu.neptunus.olympus.custom.network.controller.SocketNetwork;

public class MapActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    private static String TAG = MapActivity.class.getName();

    private static MountainInfoData mountainInfoData = null;

    public static void setMountainInfoData(MountainInfoData mountainInfoData) {
        MapActivity.mountainInfoData = mountainInfoData;
    }

    private Toolbar toolbar = null;

    private RelativeLayout relativeLayout = null;
    private TMapView tMapView = null;
    private TMapGpsManager tMapGpsManager = null;

    private TextView stepView = null;
    private int step = 0;
    private SensorManager sensorManager = null;
    private Sensor stepSensor = null;

    private Chronometer timeView = null;

    private LinearLayout bottomBar = null;

    private int pointLevel = 0;


    private SocketNetwork network = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (mountainInfoData == null) {
            Toast.makeText(this, "에러가 발생했습니다.\n계속해서 에러가 발생할 경우 알려주세요.", Toast.LENGTH_SHORT).show();
            finish();
        }

        initToolbar();
        initTMap();
        checkGps(true);
        initStepCounter();
        initTimer();
        initBottomBar();

        initConnection();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        checkGps(false);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(mountainInfoData.getName());
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
        tMapView.addTMapPath(tMapPolyLine);
        tMapView.showFullPath(tMapPolyLine);
        tMapView.MapZoomOut();
    }

    private void checkGps(boolean alert) {
        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1.0f, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "last loc = " + locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }, 2000);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (alert) {
                showGpsAlert();
            } else {
                initTMapGpsManager(false);
            }
        } else {
            initTMapGpsManager(true);
        }
    }

    private Runnable repeat = null;

    private void initTMapGpsManager(boolean isGps) {
        tMapGpsManager = new TMapGpsManager(this);
        tMapGpsManager.setMinTime(100);
        tMapGpsManager.setMinDistance(0.01f);
        tMapGpsManager.setProvider(isGps ? TMapGpsManager.GPS_PROVIDER : TMapGpsManager.NETWORK_PROVIDER);
        tMapGpsManager.setLocationCallback();
        tMapGpsManager.OpenGps();
        tMapGpsManager.getLocation();
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
                initTMapGpsManager(false);
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
                    if (gapTime > 400) {
                        lastTime = currentTime;
                        x = Math.abs(event.values[SensorManager.DATA_X]);
                        y = Math.abs(event.values[SensorManager.DATA_Y]);
                        z = Math.abs(event.values[SensorManager.DATA_Z]);

                        speed = (float) Math.sqrt((double)((x-lastX)*(x-lastX)+(y-lastY)*(y-lastY)+(z-lastZ)*(z-lastZ)))/gapTime;

                        if (speed > 0.04f) {
                            step++;
                            stepView.setText("" + step);
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
            public void useData(JSONObject jsonObject) {
                Log.d(TAG, jsonObject.toString());
            }
        });
        network.sendData("This is a String.");
    }

    @Override
    public void onLocationChange(Location location) {
        Log.d(TAG, location.toString());
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.forceLayout();
        relativeLayout.forceLayout();


    }

    private int closestPoint(TMapPoint[] points, Location current) {
        float distance = 0;
        TMapPolyLine tMapPolyLine = new TMapPolyLine();
        return 0;
    }
}