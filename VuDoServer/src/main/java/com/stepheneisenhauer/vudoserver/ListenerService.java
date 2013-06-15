package com.stepheneisenhauer.vudoserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.androidzeitgeist.ani.discovery.Discovery;
import com.androidzeitgeist.ani.discovery.DiscoveryException;
import com.androidzeitgeist.ani.discovery.DiscoveryListener;

import java.net.InetAddress;

/**
 * Created by stephen on 6/15/13.
 */
public class ListenerService extends Service {
    private static final String TAG = "ListenerService";

    @Override
    public void onCreate() {
        super.onCreate();

        // (1) Implement a listener

        DiscoveryListener listener = new DiscoveryListener() {
            public void onDiscoveryStarted() {
                // The discovery has been started in the background and is now waiting
                // for incoming Intents.

                //Toast.makeText(ListenerService.this, "Now listening for VuDu events", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Now listening for VuDo events");
            }

            public void onDiscoveryStopped() {
                // The discovery has been stopped. The listener won't be notified for
                // any incoming Intents anymore.

                //Toast.makeText(ListenerService.this, "VuDu discovery has stopped", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "VuDo discovery has stopped");
            }

            public void onDiscoveryError(Exception exception) {
                // A (network) error has occurred that prevents the discovery from working
                // probably. The actual Exception that has been thrown in the background
                // thread is passed to this method. A call of this method is almost always
                // followed by a call to onDiscoveryStopped()

                //Toast.makeText(ListenerService.this, "Error with VuDu discovery", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error with VuDo discovery");
            }

            public void onIntentDiscovered(InetAddress address, Intent intent) {
                // An Intent has been successfully received from the given address.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(null);
                startActivity(intent);
                Log.i(TAG, "Received a VuDo intent; Launching it now");
            }
        };

        // (2) Create and start a discovery

        Discovery discovery = new Discovery();
        discovery.setDisoveryListener(listener);

        try {
            discovery.enable(); // Start discovery
        } catch (DiscoveryException exception) {
            Toast.makeText(ListenerService.this, "Error enabling VuDo discovery", Toast.LENGTH_SHORT).show();
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
