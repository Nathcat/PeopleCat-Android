package com.nathcat.peoplecat_client_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nathcat.peoplecat_client_android.networking.NetworkerService;

public class AutoStartService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startForegroundService(new Intent(context, NetworkerService.class));
    }
}