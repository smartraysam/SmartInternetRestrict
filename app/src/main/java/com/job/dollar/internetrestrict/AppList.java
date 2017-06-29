package com.job.dollar.internetrestrict;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by SMARTTECHX on 11/17/2016.
 */

public class AppList implements Comparable<AppList> {
    public PackageInfo info;
    public String name;
    public boolean wifi_blocked;
    public boolean mobile_blocked;

    private AppList(PackageInfo info, boolean wifi_blocked, boolean mobile_blocked, Context context) {
        this.info = info;
        this.name = info.applicationInfo.loadLabel(context.getPackageManager()).toString();
        this.wifi_blocked = wifi_blocked;
        this.mobile_blocked = mobile_blocked;
    }

    public static List<AppList> getAppLists(Context context) {
        SharedPreferences wifi = context.getSharedPreferences("wifi", Context.MODE_PRIVATE);
        SharedPreferences mobile = context.getSharedPreferences("mobile", Context.MODE_PRIVATE);

        List<AppList> listApps = new ArrayList<>();
        for (PackageInfo info : context.getPackageManager().getInstalledPackages(0))
            listApps.add(new AppList(
                    info,
                    wifi.getBoolean(info.packageName, false),
                    mobile.getBoolean(info.packageName, false),
                    context
            ));

        Collections.sort(listApps);

        return listApps;
    }

    public Drawable getIcon(Context context) {
        return info.applicationInfo.loadIcon(context.getPackageManager());
    }

    @Override
    public int compareTo(AppList mobile) {
        int i = name.compareToIgnoreCase(mobile.name);
        return (i == 0 ? info.packageName.compareTo(mobile.info.packageName) : i);
    }
}
