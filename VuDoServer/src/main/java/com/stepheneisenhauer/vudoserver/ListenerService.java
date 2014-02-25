package com.stepheneisenhauer.vudoserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;

/**
 * The ListenerService Service operates an HTTP server which listens for certain HTTP requests
 * and takes appropriate action when they are received (e.g. opening a URL sent from a client).
 */
public class ListenerService extends Service {
    static final String TAG = "VuDo/ListenerService";
    String mServiceName = "VuDo";
    String mServiceType = "_vudo._tcp.";
    int mLocalPort;
    AsyncHttpServer server = new AsyncHttpServer();
    NsdManager mNsdManager;
    NsdManager.RegistrationListener mRegistrationListener;
    Handler handler;

    static final String TYPE_TEXT = "text/plain";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();

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
                try {
                    // Serve the index.html page
                    InputStream indexStream = getResources().openRawResource(R.raw.index);
                    BufferedReader r = new BufferedReader(new InputStreamReader(indexStream));
                    StringBuilder total = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line);
                    }
                    response.send(total.toString());
                    return;
                } catch (IOException e) {
                    response.responseCode(500);
                    response.send(TYPE_TEXT, "There was a problem serving this page :(");
                }
            }
        });
        server.post("/view", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                // Interpret the POST request and take action
                JSONObjectBody body = (JSONObjectBody)request.getBody();
                JSONObject json = body.get();
                try {
                    String type = json.getString("type");
                    if (type.equals("uri")) {
                        // Open the URI using an ACTION_VIEW Intent
                        Uri uri = Uri.parse(json.getString("uri"));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setComponent(null);
                        startActivity(intent);
                        response.send(TYPE_TEXT, "The URI has been opened on the device.");
                        return;
                    }
                    else if (type.equals("text")) {
                        // Display text as a Toast notification
                        final String text = json.getString("text");
                        handler.post(new Runnable() {
                           public void run() {
                               Toast toast = Toast.makeText(ListenerService.this, text, Toast.LENGTH_LONG);
                               toast.show();
                           }
                        });
                        response.send(TYPE_TEXT, "The message has been displayed on the device.");
                        return;
                    }
                    else if (type.equals("image")) {
                        // TODO
                        response.responseCode(400);
                        response.send(TYPE_TEXT, "This content type isn't supported yet.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // If we've reached this point, the user must have sent us something we don't know
                // how to handle. Return a HTTP 400 response.
                response.responseCode(400);
                response.send(TYPE_TEXT, "Your request was bad, and you should _feel_ bad.");
            }
        });

        // Start listening on the selected port
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
                Log.d(TAG, "Successfully unregistered network service.");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
                Log.e(TAG, "Network service unregistration failed! Error code: " + errorCode);
            }
        };
    }

    private void registerNetworkService() {
        initializeRegistrationListener();

        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(android.os.Build.MODEL);
        serviceInfo.setServiceType(mServiceType);
        serviceInfo.setPort(mLocalPort);

        // Grab the NSD Manager instance
        mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);

        // Register the discovery service using the NSD Manager
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    private void unregisterNetworkService() {
        mNsdManager.unregisterService(mRegistrationListener);
        Toast.makeText(this, "VuDo: Receive is stopping...", Toast.LENGTH_SHORT).show();
    }


    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ListenerService is being destroyed.");
        server.stop();
        unregisterNetworkService();
    }
}
