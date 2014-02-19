package com.stepheneisenhauer.vudoserver;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by stephen on 6/15/13.
 */
public class ListenerService extends Service {
    static final String TAG = "VuDo/ListenerService";
    String mServiceName = "VuDo";
    int mLocalPort;
    AsyncHttpServer server = new AsyncHttpServer();
    NsdManager mNsdManager;
    NsdManager.RegistrationListener mRegistrationListener;

    @Override
    public void onCreate() {
        super.onCreate();

        // Find a free port to use
        try {
            ServerSocket ss = new ServerSocket(0);
            mLocalPort = ss.getLocalPort();
            ss.close();
        } catch (IOException e) {
            mLocalPort = 9600;
        }

        // Define API endpoints
        server.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.send("VuDo: Receive is active.");
            }
        });
        server.post("/view", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                // Interpret the POST request and take action
                // TODO
                //AsyncHttpRequestBody body = request.getBody();

            }
        });

        // Start listening on port 5000
        server.listen(mLocalPort);
        Log.d(TAG, "Listening on port " + mLocalPort);

        // Register the service on the network for discovery (Android 4.1+)
        registerNetworkService();
    }

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
        serviceInfo.setServiceName(android.os.Build.MODEL);
        serviceInfo.setServiceType("_vudo._tcp.");
        serviceInfo.setPort(mLocalPort);

        // Register the discovery service using the NSD Manager
        mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }


    public IBinder onBind(Intent intent) {
        return null;
    }
}
