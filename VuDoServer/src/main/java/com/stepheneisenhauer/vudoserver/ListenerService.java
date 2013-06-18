package com.stepheneisenhauer.vudoserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.androidzeitgeist.ani.discovery.Discovery;
import com.androidzeitgeist.ani.discovery.DiscoveryException;
import com.androidzeitgeist.ani.discovery.DiscoveryListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Created by stephen on 6/15/13.
 */
public class ListenerService extends Service {
    static final String TAG = "ListenerService";
    String mServiceName = "VuDo";
    NsdManager.RegistrationListener mRegistrationListener;
    NsdManager mNsdManager;
    int port;

    @Override
    public void onCreate() {
        super.onCreate();

        // Find a free port to use
        port = 0;
        try {
            ServerSocket ss = new ServerSocket(0);
            port = ss.getLocalPort();
            ss.close();
        } catch (IOException e) {
            port = 9600;
        }
        port = 9600;

        // Start the HTTP server
        HttpServer httpd = new HttpServer(port, this);
        try {
            httpd.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Register the service on the network for discovery
        registerNetworkService();

        /*
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
        */
    }

    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
            }
        };
    }

    private void registerNetworkService() {
        initializeRegistrationListener();

        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);

        // Register the discovery service using the NSD Manager
        mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
