package se.fredrike.beantemp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;
import com.punchthrough.bean.sdk.BeanListener;
import com.punchthrough.bean.sdk.BeanManager;
import com.punchthrough.bean.sdk.message.BatteryLevel;
import com.punchthrough.bean.sdk.message.BeanError;
import com.punchthrough.bean.sdk.message.Callback;
import com.punchthrough.bean.sdk.message.ScratchBank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fer on 16-12-15.
 */
public class Worker extends IntentService {
    public Worker() {
        super("SchedulingService");
    }
    private static String API = "";
    private static final String EMONCMS = "https://www.emoncms.org/input/post.json?apikey=" + API + "&";

    private NotificationManager mNotificationManager;


    private final List<Bean> beans = new ArrayList<>();
    private final Worker mWorker = this;
    private TextView textView = null;
    private final List<String> bleAddressList = new ArrayList<>();
    private int totalThreads = 0;

    class MyThread implements Runnable {

        final TextView tv;
        final String mac;
        final Bean b;
        final Thread th;
        int temp, battery;

        MyThread(TextView tv1, String mac1, Bean b1) {
            tv = tv1;
            mac = mac1;
            b = b1;
            temp = 255;
            battery = 255;
            th = new Thread(this);
            th.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            th.start();
        }

        @Override
        public void run() {
            b.readTemperature(new Callback<Integer>() {
                @Override
                public void onResult(Integer result) {
                    temp = result;
                    if (battery != 255) {
                        pushData();
                        temp = 255;
                        battery = 255;
                    }
                }
            });
            b.readBatteryLevel(new Callback<BatteryLevel>() {
                @Override
                public void onResult(BatteryLevel result) {
                    battery = result.getPercentage();
                    if (temp != 255) {
                        pushData();
                        temp = 255;
                        battery = 255;
                    }
                }
            });
        }

        private void pushData() {
            String name = b.getDevice().getName();
            String data = "\uD83C\uDF21 " + temp + "°,  \uD83D\uDD0B " + battery + "%" ;
            String msg = name + " " + mac + ": " + " " + data;
            b.disconnect();
            if (tv != null) {
                tv.append("\n" + msg);
                tv.invalidate();
            }

            System.out.println(msg);
            try {
                sendNotification(name + " " + mac.replace(":", ""), Long.parseLong(mac.replace(":", ""), 16), data);
            } catch (Exception e) { System.out.print(e.toString());}

            String urlString = EMONCMS + "json={" + "\"uptime\":" + (SystemClock.elapsedRealtime() / 1000) + "," +
                    "\"" + mac.replace(":", "") + "-temp\":" + temp + ",\"" + mac.replace(":", "") + "-battery\":" + battery + "}";
            new urlRequest().execute(urlString);
            System.out.println("url: " + urlString);
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        System.out.println("Alarm Triggered");
        beans.clear();
        BeanManager.getInstance().startDiscovery(listener);
    }

    public void runOnce(TextView tw) {
        textView = tw;
        beans.clear();
        BeanManager.getInstance().startDiscovery(listener);
    }

    private final BeanDiscoveryListener listener = new BeanDiscoveryListener() {
        @Override
        public void onBeanDiscovered(Bean bean, int rssi) {
            beans.add(bean);
            bean.connect(mWorker, beanListener);
        }

        @Override
        public void onDiscoveryComplete() {
            System.out.println("Total beans discovered: " + beans.size());
        }
    };

    private final BeanListener beanListener = new BeanListener() {
        @Override
        public void onConnected() {
            if (textView != null) {
                textView.append("\nA new bean has been connected.");
            }
            System.out.println("A bean has connected.");
            System.out.println("Total bean: " + beans.size());
            System.out.println("Total threads started so far: " + totalThreads);

            for (int i = 0; i < beans.size(); i++) {
                Bean b = beans.get(i);
                String baddr = b.getDevice().getAddress();
                if (b.isConnected()) {
                    if (!bleAddressList.contains(baddr)) {
                        bleAddressList.add(baddr);
                        ++totalThreads;
                        new MyThread(textView, baddr, b);
                        System.out.println("Total threads started now: " + totalThreads);
                        return;
                    }
                }
            }
        }

        @Override
        public void onError(BeanError berr) {
            System.out.println("Bean has errors..");
        }

        @Override
        public void onConnectionFailed() {
            System.out.println("Bean connection failed");
        }

        @Override
        public void onDisconnected() {
            System.out.println("Bean disconnected");
        }

        @Override
        public void onScratchValueChanged(ScratchBank bank, byte[] value) {
//            System.out.println("Bean scratch value changed");
        }

        @Override
        public void onSerialMessageReceived(byte[] data) {
//            System.out.println("data received: " + data.toString());
        }

    };

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String title, long mId, String text) {
        if (mNotificationManager == null) {
            return;
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(false)
                        .setSmallIcon(R.drawable.temperature_icon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setGroup(String.valueOf(mId))
                ;

        //=  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) mId, mBuilder.build());
    }


//
// The methods below this line fetch content from the specified URL and return the
// content as a string.
//
    private class urlRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return loadFromNetwork(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
        }

        /**
         * Given a URL string, initiate a fetch operation.
         */
        private String loadFromNetwork(String urlString) throws IOException {
            InputStream stream = null;
            String str = "";

            try {
                stream = downloadUrl(urlString);
                str = readIt(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return str;
        }

        /**
         * Given a string representation of a URL, sets up a connection and gets
         * an input stream.
         *
         * @param urlString A string representation of a URL.
         * @return An InputStream retrieved from a successful HttpURLConnection.
         * @throws IOException
         */
        private InputStream downloadUrl(String urlString) throws IOException {

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Start the query
            conn.connect();
            return conn.getInputStream();
        }

        /**
         * Reads an InputStream and converts it to a String.
         *
         * @param stream InputStream containing HTML from www.google.com.
         * @return String version of InputStream.
         * @throws IOException
         */
        private String readIt(InputStream stream) throws IOException {

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            for (String line = reader.readLine(); line != null; line = reader.readLine())
                builder.append(line);
            reader.close();
            return builder.toString();
        }

    }
}
