package com.job.dollar.internetrestrict;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.util.Log;

/**
 * Created by SMARTTECHX on 11/17/2016.
 */

public class Receiver  extends BroadcastReceiver {
    private static final String TAG = "Internet.Boot";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);

        if (VpnService.prepare(context) == null) {
            Intent service = new Intent(context, VPNInitService.class);
            service.putExtra(VPNInitService.EXTRA_COMMAND, VPNInitService.Command.start);
            context.startService(service);
        }
    }
}
