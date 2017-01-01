package se.fredrike.beantemp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;


public class BeanTempActivity extends AppCompatActivity {
    private final se.fredrike.beantemp.AlarmReceiver alarm = new AlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beantemp);
        TextView textView = (TextView) findViewById(R.id.text1);
        textView.setMovementMethod(new ScrollingMovementMethod());
        //BeanManager.getInstance().startDiscovery(listener);
        alarm.setAlarm(this);
        Worker w = new Worker();
        w.runOnce(textView);
    }
}
