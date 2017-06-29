package com.job.dollar.internetrestrict;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by SMARTTECHX on 11/17/2016.
 */

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> implements Filterable{
    private static final String TAG = "Applist.List";

    private List<AppList> listAll;
    private List<AppList> listSelected;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ImageView appIcon;
        public TextView appName;
        public TextView appPackage;
        public CheckBox Wifi;
        public CheckBox Mobile;


        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            appIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
            appName = (TextView) itemView.findViewById(R.id.tvName);
            appPackage = (TextView) itemView.findViewById(R.id.tvPackage);
            Wifi = (CheckBox) itemView.findViewById(R.id.cbWifi);
            Mobile = (CheckBox) itemView.findViewById(R.id.cbMobile);


        }
    }

    public ApplicationAdapter(List<AppList> listApp) {
        listAll = listApp;
        listSelected = new ArrayList<>();
        listSelected.addAll(listApp);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,  int position) {
        final AppList applist  = listSelected.get(position);

        holder.appIcon.setImageDrawable(applist.getIcon(holder.view.getContext()));
        holder.appName.setText(applist.name);
        holder.appPackage.setText(applist.info.packageName);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent InfoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                InfoIntent.setData(Uri.parse("package:"+applist.info.packageName));
                context.startActivity(InfoIntent);
            }
        });


        CompoundButton.OnCheckedChangeListener cbListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String name;
                if (buttonView == holder.Wifi) {
                    name = "wifi";
                    applist.wifi_blocked = isChecked;
                } else {
                    name = "mobile";
                    applist.mobile_blocked = isChecked;
                }
                Log.i(TAG, applist.info.packageName + ": " + name + "=" + isChecked);

                Context context = buttonView.getContext();

                SharedPreferences prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
                prefs.edit().putBoolean(applist.info.packageName, isChecked).apply();

                Intent intent = new Intent(context, VPNInitService.class);
                intent.putExtra(VPNInitService.EXTRA_COMMAND, VPNInitService.Command.reload);
                context.startService(intent);
            }
        };

        holder.Wifi.setOnCheckedChangeListener(null);
        holder.Wifi.setChecked(applist.wifi_blocked);
        holder.Wifi.setOnCheckedChangeListener(cbListener);

        holder.Mobile.setOnCheckedChangeListener(null);
        holder.Mobile.setChecked(applist.mobile_blocked);
        holder.Mobile.setOnCheckedChangeListener(cbListener);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                List<AppList> listResult = new ArrayList<>();
                if (query == null)
                    listResult.addAll(listAll);
                else {
                    query = query.toString().toLowerCase();
                    for (AppList applist : listAll)
                        if (applist.name.toLowerCase().contains(query))
                            listResult.add(applist);
                }

                FilterResults result = new FilterResults();
                result.values = listResult;
                result.count = listResult.size();
                return result;
            }

            @Override
            protected void publishResults(CharSequence query, FilterResults result) {
                listSelected.clear();
                if (result == null)
                    listSelected.addAll(listAll);
                else
                    for (AppList applist : (List<AppList>) result.values)
                        listSelected.add(applist);
                notifyDataSetChanged();
            }
        };
    }

    public void toggle(String name, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        for (AppList applist: listSelected) {
            if ("wifi".equals(name)) {
                applist.wifi_blocked = !applist.wifi_blocked;
                editor.putBoolean(applist.info.packageName, applist.wifi_blocked);
            } else {
                applist.mobile_blocked = !applist.mobile_blocked;
                editor.putBoolean(applist.info.packageName, applist.mobile_blocked);
            }
        }
        editor.apply();

        Intent intent = new Intent(context, VPNInitService.class);
        intent.putExtra(VPNInitService.EXTRA_COMMAND, VPNInitService.Command.reload);
        context.startService(intent);

        notifyDataSetChanged();
    }

    @Override
    public ApplicationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.applist, parent, false));
    }

    @Override
    public int getItemCount() {
        return listSelected.size();
    }
}
