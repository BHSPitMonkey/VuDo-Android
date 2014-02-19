package com.stepheneisenhauer.vudo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by stephen on 6/15/13.
 */
public class ClientActivity extends Activity {
    private static final String TAG = "VuDo/ClientActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        //Log.d(TAG, "Intent data URI is:");
        //Log.d(TAG, intent.getDataString());
        //Log.d(TAG, "Intent extra text is:");
        //Log.d(TAG, intent.getStringExtra(Intent.EXTRA_TEXT));

        // Discover VuDo servers
        // TODO

        // If no servers found, just show an appropriate message and return
        // TODO
        if (false) {
            String message = "No VuDo servers found.";
            Toast toast = Toast.makeText(ClientActivity.this, message, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // Decide how to handle the intent
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                // We're dealing with a plaintext SEND intent.
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (text == null || text.isEmpty()) {
                    Log.e(TAG, "Unable to get the EXTRA_TEXT from the SEND intent. Giving up.");
                    return;
                }

                // If the EXTRA_TEXT field looks like a URI...
                try {
                    Uri uri = Uri.parse(text);
                    // Note: I'm not sure if the URI is guaranteed to be valid at this point
                    // Send a HTTP POST request to the selected server(s):
                    // TODO
                } catch (Exception e) {
                    Log.d(TAG, "There was an exception parsing the text as a URI.");
                }
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
}

