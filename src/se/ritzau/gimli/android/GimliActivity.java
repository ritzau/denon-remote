package se.ritzau.gimli.android;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import se.ritzau.gimli.AVRUI;
import se.ritzau.gimli.DenonAVR;
import se.ritzau.gimli.Log;
import se.ritzau.gimli.R;
import se.ritzau.gimli.SocketConnection;
import se.ritzau.ui.ValueListener;
import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.widget.Toast;

public class GimliActivity extends Activity implements AVRUI {
    private DenonAVR denon;
    private Thread connectionThread;
    private MarvinVolumeControl volumeControl;
    private long lastVibration = 0;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
        wifi.setWifiEnabled(true);
        
        volumeControl = (MarvinVolumeControl) findViewById(R.id.volume);
	volumeControl.setValueListener(new ValueListener() {
	    @Override public void onValueChanged(float value) { 
		if (denon != null) denon.setMasterVolume(value);
		Log.i("vol: " + value);
	
		long now = SystemClock.currentThreadTimeMillis();
		if (lastVibration + 20 < now) {
		    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(20);
		    lastVibration = now;
		}
	    }
	});

	try {
	    // XXX make the connection thread owned by a thread or pass it in the icicle
	    SocketConnection connection = new SocketConnection("gimli.local.ritzau.se");
	    denon = new DenonAVR(this, connection);

	    connectionThread = new Thread(connection);
	    connectionThread.start();
		
	    denon.turnOn();
	} catch (SocketTimeoutException e) {
	    Toast.makeText(this, "Can't connect to host", Toast.LENGTH_LONG).show();
	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    @Override
    public void onVolumeUpdated(float param) {
	Log.i("Updated: " + "vol" + " " + param);
	// XXX if user is in control then save the last value and set it as soon as the user lets go (notification?)
	if (volumeControl != null && !volumeControl.isControlledByUser()) {
	    volumeControl.setVolume(param);
	}
    }

    @Override
    public void onPowerStateUpdated(boolean value) {
	Log.i("Updated: " + "pow" + " " + value);
    }
}