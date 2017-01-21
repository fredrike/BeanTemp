package se.fredrike.beantemp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;



public class BeanTempActivity extends AppCompatActivity {
    private final se.fredrike.beantemp.AlarmReceiver alarm = new AlarmReceiver();

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 271;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)  != PackageManager.PERMISSION_GRANTED)
            showPermissionRequestDialog();
        else
            discoverBeans();
        alarm.setAlarm(this);
    }

    protected void showSettings(View view) {
        Intent modifySettings=new Intent(BeanTempActivity.this,SettingsActivity.class);
        startActivity(modifySettings);
    }

    private void discoverBeans() {
        setContentView(R.layout.activity_beantemp);
        TextView textView = (TextView) findViewById(R.id.text1);
        textView.setText("Starting BlueBean discovery...");
        Worker w = new Worker();
        Context ctx = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        w.run(textView, prefs);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showPermissionRequestDialog() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    discoverBeans();
                }
            }
        }
    }
}
