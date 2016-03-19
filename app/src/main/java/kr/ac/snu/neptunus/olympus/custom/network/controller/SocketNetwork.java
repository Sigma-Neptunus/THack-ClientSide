package kr.ac.snu.neptunus.olympus.custom.network.controller;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by jjc93 on 2016-03-18.
 */
public class SocketNetwork {
    private static final String TAG = SocketNetwork.class.getName();

    private static final String ServerIP = "147.46.242.59";
    private static final int ServerPort = 8001;

    private Socket socket = null;

    private Boolean edisonConnection = false;

    public void setSocket() {
        try {
            edisonConnection = false;
            socket = IO.socket("http://"+ServerIP+":"+ServerPort);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void emitAdduser() {
        final String connectEvent = "connect";
        final String adduserEvent = "adduser";
        final String userType = "mobile";
        if (socket != null) {
            socket.on(connectEvent, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "TESTTT");
                    socket.emit(adduserEvent, userType);
                    catchUpdateuser();
                }
            });
        }
    }

    public void catchUpdateuser() {
        final String updateuserEvent = "update";
        if (socket != null) {
            socket.on(updateuserEvent, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject jsonObjects = (JSONObject) args[0];
                    Log.d(TAG, jsonObjects.toString());
                    if (!jsonObjects.optBoolean("connection")) {
                        Log.e(TAG, "Error at adduser");
                        emitAdduser();
                    } else {
                        edisonConnection = jsonObjects.optBoolean("edison");
                    }
                }
            });
        }
    }

    public void defaultSettings() {
        setSocket();
        emitAdduser();
        socket.connect();
    }

    public void sendData(String value) {
        final String sendDataEvent = "To_edison";
        if (socket != null) {
            Log.d(TAG, "Data to edison");
            socket.emit(sendDataEvent, value);
        }
    }

    OnDataListener fromEdisonDataListener = null;

    public void setOnDataListener(final OnDataListener onDataListener) {
        final String getDataEvent = "From_edison";
        if (socket != null) {
            Log.d(TAG, "Get Data");
            socket.on(getDataEvent, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Data from edison");
                    if (onDataListener != null) {
                        onDataListener.useData((JSONObject) args[0]);
                    }
                }
            });
        }
    }

    public interface OnDataListener {
        public void useData(JSONObject jsonObject);
    }
}
