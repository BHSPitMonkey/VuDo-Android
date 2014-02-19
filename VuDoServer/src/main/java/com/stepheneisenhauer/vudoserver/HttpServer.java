/*
package com.stepheneisenhauer.vudoserver;


import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.Properties;


public class HttpServer extends AndroidHTTPD {
    private static final String TAG = "VuDo/HttpServer";
    ListenerService service;

    public HttpServer(int port, ListenerService caller) throws IOException {
        super(caller, port, null, null);
        service = caller;
    }

    @Override
    public NanoHTTPDPooled.Response serve(String uri, String method, Properties header,
                          Properties parms, Properties files) {
        Log.d(TAG, "Request received with URI: ");
        Log.d(TAG, uri);
        // Handle URI "view"
        if (uri.equals("/view")) {
            String type = parms.getProperty("type");
            if (type != null) {
                if (type.equals("uri")) {
                    String uriString = parms.getProperty("uri");
                    if (uriString != null && !uriString.equals("")) {
                        // TODO: Validate this URI better
                        try {
                            Uri uriObj = Uri.parse(uriString);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uriObj);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setComponent(null);
                            service.startActivity(intent);
                            Log.i(TAG, "Launching a VIEW intent now.");
                            return okResponse();
                        } catch (Exception e) {
                            Log.e(TAG, "Caught an exception while parsing/displaying a URI");
                            return badRequestResponse();
                        }
                    }
                } // end if type==uri
                else if (type.equals("image")) {
                    // TODO
                } // end if type==image
            } // end if type param != null
            return badRequestResponse();
        }

        return notFoundResponse();
    }

    Response okResponse() {
        return new Response(HTTP_OK, MIME_PLAINTEXT, "");
    }

    Response notFoundResponse() {
        return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "");
    }

    Response badRequestResponse() {
        return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "");
    }
}
 */