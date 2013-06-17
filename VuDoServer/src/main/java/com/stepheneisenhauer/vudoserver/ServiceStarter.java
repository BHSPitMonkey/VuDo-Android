package com.stepheneisenhauer.vudoserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This BroadcastReceiver is invoked whenever the system boots (ACTION_BOOT_COMPLETED).
 * Its purpose is to start the ListenerService service. And that's exactly what it does.
 *
 * Created by stephen on 6/15/13.
 */
public class ServiceStarter extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, ListenerService.class);
        context.startService(myIntent);
    }
}
