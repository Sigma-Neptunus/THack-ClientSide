package kr.ac.snu.neptunus.olympus;

import android.app.Application;
import android.util.Log;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by jjc93 on 2016-03-19.
 */
public class app extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("a", "b");
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }
}
