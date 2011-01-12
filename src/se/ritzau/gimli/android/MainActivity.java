package se.ritzau.gimli.android;

import java.io.IOException;
import java.net.UnknownHostException;

import se.ritzau.gimli.AVRUI;
import se.ritzau.gimli.DenonAVR;
import se.ritzau.gimli.Log;
import se.ritzau.gimli.R;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class MainActivity extends Activity implements AVRUI {
    private WifiManager wifiManager;
//    private ConnectivityManager connectivityManager;

    private final static int MSG_TURN_ON_WIFI = 0;
    private final static int MSG_CONNECT_TO_AMP = 1;

    private BroadcastReceiver wifiStateHandler = new BroadcastReceiver() {
	public void onReceive(Context context, Intent intent) {
	    Log.i("FOO: " + intent);
	    if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED) {
		dismissDialog(DLG_CONNECT);
	    }
	}
    };
    
    @SuppressWarnings("unused")
    private BroadcastReceiver connectivityStateHandler = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();
	    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
	    
	    Log.i("action: " + action);
	    Log.i("info: " + intent.getStringExtra(ConnectivityManager.EXTRA_EXTRA_INFO));
	    Log.i("network: " + networkInfo.getType());
	    Log.i("connected: " + networkInfo.isConnected());
	    
	    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
		handler.sendEmptyMessage(MSG_CONNECT_TO_AMP);
	    }
        }
    };
    
    private class AsyncHandler extends Handler {
	private AsyncHandler() {
	    super(handlerThread.getLooper());
	}

	public void handleMessage(Message msg) {
	    Log.i("Got message: " + msg.what);

	    switch (msg.what) {
	    case MSG_TURN_ON_WIFI:
		if (!wifiManager.setWifiEnabled(true)) {
		    Log.i("Couldn't turn wifi on");
		}
		Log.i("enabled: " + wifiManager.isWifiEnabled() + " " + wifiManager.getWifiState());
		break;

	    case MSG_CONNECT_TO_AMP:
		try {
		    denon = new DenonAVR(MainActivity.this, "gimli.local.ritzau.se");
		} catch (UnknownHostException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		break;

	    default:
		throw new AssertionError();
	    }
	}
    };

    private HandlerThread handlerThread = new HandlerThread("Main activity thread");
    private Handler handler = null;

    private DenonAVR denon;
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
//      connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        
        handlerThread.start();
        handler = new AsyncHandler();
        
        registerReceiver(wifiStateHandler, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
//        registerReceiver(connectivityStateHandler, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        
	if (!wifiManager.setWifiEnabled(true)) {
	    Log.i("Couldn't turn wifi on");
	}
	
        denon = (DenonAVR) getLastNonConfigurationInstance();
        if (denon == null || !denon.isConnected()) {
//            showDialog(Dialogs.CONNECT.ordinal());
            
            Log.i("enabled: " + wifiManager.isWifiEnabled() + " " + wifiManager.getWifiState());
            
            if (!wifiManager.isWifiEnabled()) {
        	Log.i("Trying to enable");
        	handler.sendEmptyMessage(MSG_TURN_ON_WIFI);
            }
        }
        
        setContentView(R.layout.main);
    }

    private final static int DLG_CONNECT = 0;
    
    @Override
    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case DLG_CONNECT:
	    ProgressDialog dlg = ProgressDialog.show(this, "Connecting", "bla bla bla...");
	    dlg.setCancelable(true);
	    return dlg;
	    
	default:
	    throw new AssertionError();
	}
    }
    
    @Override
    public void onBackPressed() {
	Log.i("Back");
	finish();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        return denon;
    }
    
    @Override
    protected void onDestroy() {
	Log.i("On destroy");
	
        super.onDestroy();

        if (!wifiManager.setWifiEnabled(false)) {
            Log.i("Couldn't turn wifi off");
        }

// XXX when should we shut down?
//        try {
//	    connection.close();
//	} catch (IOException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}

	handlerThread.quit();
	
	unregisterReceiver(wifiStateHandler);
//        unregisterReceiver(connectivityStateHandler);
    }

    @Override
    public void onVolumeUpdated(float param) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onPowerStateUpdated(boolean value) {
	// TODO Auto-generated method stub
	
    }
}
