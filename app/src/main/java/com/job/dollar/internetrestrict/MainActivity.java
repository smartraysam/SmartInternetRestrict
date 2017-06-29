package com.job.dollar.internetrestrict;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "Main";
    private boolean running = false;
    private ApplicationAdapter adapter = null;
    RecyclerView ApplicationList;
    private static final int REQUEST_VPN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        running = true;

        // Action bar
        View view = getLayoutInflater().inflate(R.layout.actionbar, null);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(view);
        setTitle("Favourites");

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ApplicationList = (RecyclerView) findViewById(R.id.AppList);
        ApplicationList.setHasFixedSize(true);
        ApplicationList.setLayoutManager(new LinearLayoutManager(this));
        // On/off switch
        Switch swEnabled = (Switch) view.findViewById(R.id.swVPN);
        swEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "Switch on");
                    Intent intent = VpnService.prepare(MainActivity.this);
                    if (intent == null) {
                        Log.e(TAG, "Prepare done");
                        onActivityResult(REQUEST_VPN, RESULT_OK, null);
                    } else {
                        Log.i(TAG, "Start intent=" + intent);
                        startActivityForResult(intent, REQUEST_VPN);
                    }
                } else {
                    Log.i(TAG, "Switch off");
                    prefs.edit().putBoolean("enabled", false).apply();
                    Intent intent = new Intent(MainActivity.this, VPNInitService.class);
                    intent.putExtra(VPNInitService.EXTRA_COMMAND, VPNInitService.Command.stop);
                    startService(intent);
                }
            }
        });
        swEnabled.setChecked(prefs.getBoolean("enabled", false));

        prefs.registerOnSharedPreferenceChangeListener(this);
        loadApplication();
    }


    public void loadApplication() {
        new AsyncTask<Object, Object, List<AppList>>() {
            private ProgressDialog progress = null;

            @Override
            protected List<AppList> doInBackground(Object... arg) {
                return AppList.getAppLists(MainActivity.this);
            }

            @Override
            protected void onPreExecute() {
                progress = ProgressDialog.show(MainActivity.this, null,
                        "Loading application info...");
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(List<AppList> result) {
                if (running) {
                    adapter = new ApplicationAdapter(result);
                    ApplicationList.setAdapter(adapter);
                }
                progress.dismiss();
            }
        }.execute();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String name) {
        Log.i(TAG, "Changed pref=" + name);
        if ("enabled".equals(name)) {
            boolean enabled = prefs.getBoolean(name, false);
            Switch swEnabled = (Switch) getSupportActionBar().getCustomView().findViewById(R.id.swVPN);
            if (swEnabled.isChecked() != enabled)
                swEnabled.setChecked(enabled);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroy");
        running = false;
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_clear:
                if (adapter != null)
                    ApplicationList.setAdapter(null);
                return true;
            case R.id.menu_mark:
                if (adapter != null) {
                    adapter.toggle("wifi", this);
                    adapter.toggle("mobile", this);
                }
                return true;

            case R.id.menu_refresh:
                ApplicationList.setAdapter(null);
                loadApplication();
                return true;
            case R.id.menu_help:
                Intent intenthelp = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intenthelp);
                MainActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VPN) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();

            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(MainActivity.this, VPNInitService.class);
                intent.putExtra(VPNInitService.EXTRA_COMMAND, VPNInitService.Command.start);
                startService(intent);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}

