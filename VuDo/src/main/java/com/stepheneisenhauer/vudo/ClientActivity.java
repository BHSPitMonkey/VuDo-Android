package com.stepheneisenhauer.vudo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.body.StringBody;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by stephen on 6/15/13.
 */
public class ClientActivity extends Activity {
    static final String TAG = "VuDo/ClientActivity";
    String mServiceName = "VuDo";
    String mServiceType = "_vudo._tcp.";
    NsdManager mNsdManager;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.ResolveListener mResolveListener;
    ArrayList<NsdServiceInfo> mResolvedServices;
    Handler handler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResolvedServices = new ArrayList<NsdServiceInfo>();
        handler = new Handler();

        // Begin discovering VuDo servers
        Log.d(TAG, "Starting service discovery");
        initializeDiscoveryListener();
        initializeResolveListener();
        mNsdManager.discoverServices(mServiceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

        // Set up Dialog
        DiscoveryDialogFragment df = new DiscoveryDialogFragment();
        df.show(getFragmentManager(), "DiscoveryDialog");

        // After a 1s timeout, check what has been discovered and continue
        /*
        handler.postDelayed(new Runnable() {
            public void run() {
                checkDiscoveredServers();
            }
        }, 1000);
        */

        //finish(); // when do we do this???
    }

    /**
     * Inspects the mResolvedServices set and performs the next step based on how many are inside.
     * If there are none, the user will see a Toast message indicating no servers were found.
     * If one server is found, we will begin constructing a request to that server.
     * If multiple were found, we will prompt the user to select one before proceeding.
     */
    public void checkDiscoveredServers() {
        Log.d(TAG, "Resolved services: " + mResolvedServices.size());
        // If no servers found, just show an appropriate message and return
        if (mResolvedServices.isEmpty()) {
            String message = "No VuDo servers found.";
            Toast toast = Toast.makeText(ClientActivity.this, message, Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
        else if (mResolvedServices.size() == 1) {
            prepareRequest(mResolvedServices.get(0));
        }
        else {
            // TODO: Create Dialog prompting user to select from available receivers
            // For now, send the request to all servers (yes, this is ugly/repeats too much code)
            for (NsdServiceInfo service : mResolvedServices) {
                prepareRequest(service);
            }
        }
    }

    /**
     * Interprets the Share Intent that started our Activity and tries to build a HTTP request
     * before sending that request to the specified VuDo Receiver.
     * The Activity's finish() method is called when complete.
     *
     * @param service   the NsdServiceInfo object that represents the desired VuDo Receiver
     */
    public void prepareRequest(NsdServiceInfo service) {
        HttpClient httpclient = new DefaultHttpClient();

        // Construct the API endpoint
        String serverViewUrl = "http://" + service.getHost().getHostAddress() + ":" + service.getPort() + "/view";
        Log.d(TAG, "Using server view URL: " + serverViewUrl);

        // Gather some information about the (SHARE) Intent that started us
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        // Decide how to handle the Intent
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                // We're dealing with a plaintext SEND intent.
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (text == null || text.isEmpty()) {
                    Log.e(TAG, "Unable to get the EXTRA_TEXT from the SEND intent. Giving up.");
                    return;
                }

                // First see if the EXTRA_TEXT field looks like a URI...
                try {
                    Uri uri = Uri.parse(text);
                    // Note: I'm not sure if the URI is guaranteed to be valid at this point
                    // Send a HTTP POST request to the selected server(s):
                    /*
                    AsyncHttpPost request = new AsyncHttpPost(serverViewUrl);
                    MultipartFormDataBody body = new MultipartFormDataBody();
                    body.addStringPart("type", "uri");
                    body.addStringPart("uri", uri.toString());
                    request.setBody(body);
                    AsyncHttpClient.getDefaultInstance().executeString(request); */

                    // Send a HTTP POST request to the selected server(s):
                    HttpPost post = new HttpPost(serverViewUrl);
                    try {
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                        nameValuePairs.add(new BasicNameValuePair("type", "uri"));
                        nameValuePairs.add(new BasicNameValuePair("uri", uri.toString()));
                        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        httpclient.execute(post);
                    } catch (Exception e) {
                        Log.d(TAG, "Encountered an exception during HttpPost send");
                    }

                    return;
                } catch (Exception e) {
                    Log.d(TAG, "There was an exception parsing the text as a URI.");
                }

                // Otherwise we'll just treat it as text
                AsyncHttpPost request = new AsyncHttpPost(serverViewUrl);
                MultipartFormDataBody body = new MultipartFormDataBody();
                body.addStringPart("type", "text");
                body.addStringPart("text", text);
                request.setBody(body);
                AsyncHttpClient.getDefaultInstance().executeString(request);
                return;
            }
            else if (type.startsWith("image/")) {
                // We're dealing with an image.
            }
        }
        else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                //handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        }
        else {
            // Handle other intents, such as being started from the home screen
        }

        finish();
    }

     /*
    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            CharSequence toastText;
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }
    */

    /*
    class TransmitIntentTask extends AsyncTask<Intent, Void, CharSequence> {
        // Do the long-running work in here
        protected CharSequence doInBackground(Intent... intents) {
            // Transmitter using default multicast address and port.
            Transmitter transmitter = new Transmitter();
            CharSequence toastText;
            try {
                transmitter.transmit(intents[0]);
                toastText = "Sent to VuDo servers!";
            } catch (TransmitterException exception) {
                // Handle error
                toastText = "Error sending to VuDo servers";
            }
            return toastText;
        }

        // This is called each time you call publishProgress()
        protected void onProgressUpdate() {

        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(CharSequence result) {
            //Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(ClientActivity.this, result, duration);
            toast.show();
        }
    }
    */

    /**
     * Sets up discovery of VuDo services on the network.
     */
    public void initializeDiscoveryListener() {

        mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(mServiceType)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceType().equals(mServiceType)){
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    /**
     * Sets up our listener which is called when a discovered service is resolved (after calling
     * nsdManager.resolveService(...) during discovery in onServiceFound).
     */
    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                NsdServiceInfo mService = serviceInfo;
                mResolvedServices.add(mService);
                //int port = mService.getPort();
                //InetAddress host = mService.getHost();
            }
        };
    }
}

