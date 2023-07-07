package com.singularity_code.gpstracker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.singularity_code.live_location.util.other.AlienPortal;

public class JavaAlienActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startLiveLocationService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        observeUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopObservingUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        portal.stop();
    }

    private final AlienPortal portal = new AlienPortal();

    private void startLiveLocationService() {

        portal.setGPSSamplingRate(1000L);

        portal.setContext(this);

        portal.setChannelId("Alien Notification");

        portal.setChannelName("Alien Notification");

        portal.setChannelDescription("Notification from alien app");

        portal.setupAPIorSocketURL("http://websocket.company.com?id=" + "userID-" + (Math.random() * 100));

        portal.setNotificationTitle("Live Location");

        portal.setNotificationMessage("Live Location is Running");

        // 0 -> RESTFUL
        // 1 -> Web Socket
        portal.setupNetworkMethod(1);

        // clear previous headers
        portal.clearHeader();
        portal.addHeader("Authentication", "Bearer alsdkmlam");

        portal.setMessageDescriptor(
                "You can parse anything here to describe your message. " +
                        "For example sender user id, and user name, and anything, " +
                        "in formats that you can decode."
        );

        portal.start();

        Toast.makeText(this, "Live location is running in foreground service, check your notification.", Toast.LENGTH_LONG).show();
    }


    /** # Observer **/
    private Thread observerThread;

    private boolean stopDebug = false;

    private void observeUpdate() {
        observerThread = new Thread() {

            @Override
            public void run() {

                StringBuilder sb;

                while (!stopDebug) {

                    String status = portal.getStatus();
                    String latitude = portal.getLatitude();
                    String longitude = portal.getLongitude();
                    String accuracy = portal.getAccuracy();
                    String updatedTime = portal.getUpdatedTime();
                    String altitude = portal.getAltitude();

                    sb = new StringBuilder();
                    sb.append("status : ").append(status).append("\n");
                    sb.append("latitude : ").append(latitude).append("\n");
                    sb.append("longitude : ").append(longitude).append("\n");
                    sb.append("accuracy : ").append(accuracy).append("\n");
                    sb.append("updatedTime : ").append(updatedTime).append("\n");
                    sb.append("altitude : ").append(altitude).append("\n");

                    Log.d("LiveLocation", "Current: " + sb);

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        stopDebug = false;
        observerThread.start();
    }

    private void stopObservingUpdate() {
        stopDebug = true;

        try {
            observerThread.stop();
            observerThread.destroy();
        }catch (Throwable e) {
            // nothing to do
        }
    }
}
