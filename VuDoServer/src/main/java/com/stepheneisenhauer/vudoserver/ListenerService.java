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
    static final String TAG = "VuDo/ListenerService";
    String mServiceName = "VuDo";
    NsdManager.RegistrationListener mRegistrationListener;
    NsdManager mNsdManager;
    int port;

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Move pretty much all of this into a thread

        // Find a free port to use
        /*
        port = 0;
        try {
            ServerSocket ss = new ServerSocket(0);
            port = ss.getLocalPort();
            ss.close();
        } catch (IOException e) {
            port = 9600;
        } */
        port = 9600;

        // Start the HTTP server

        try {
            HttpServer httpd = new HttpServer(port, this);
            httpd.startServer();
            Log.d(TAG, "HTTPD should be started now, on port 9600");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Register the service on the network for discovery (Android 4.1+)
        //registerNetworkService();
    }

    /*
    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "NSD service registered.");
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
                Log.d(TAG, "NSD service registration failed.");
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
    */

    public IBinder onBind(Intent intent) {
        return null;
    }
}
