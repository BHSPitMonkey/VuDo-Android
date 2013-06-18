package com.stepheneisenhauer.vudoserver;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Our custom HTTP server for handling commands from VuDu clients.
 *
 * Created by stephen on 6/17/13.
 */
public class HttpServer extends NanoHTTPD {
    private static final String TAG = "HttpServer";
    ListenerService service;

    public HttpServer(int port, ListenerService caller) {
        super(port);
        service = caller;
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
        Log.d(TAG, "Request received with URI: ");
        Log.d(TAG, uri);
        // Handle URI "view"
        if (uri.equals("view")) {
            // Handle command type type "uri"
            if (parms.get("type").equals("uri")) {
                String uriString = parms.get("uri");
                if (uriString != null) {
                    // TODO: Validate this URI
                    Uri uriObj = Uri.parse(uriString);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uriObj);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(null);
                    service.startActivity(intent);
                    Log.i(TAG, "Launching a VIEW intent now.");
                }
            }

            return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, (String) "");
        }

        return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, (String) "");
    }
}
