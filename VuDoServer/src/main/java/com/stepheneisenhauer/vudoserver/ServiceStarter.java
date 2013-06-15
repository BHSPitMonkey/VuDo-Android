package com.stepheneisenhauer.vudoserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by stephen on 6/15/13.
 */
public class ServiceStarter extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, ListenerService.class);
        context.startService(myIntent);
    }
}
