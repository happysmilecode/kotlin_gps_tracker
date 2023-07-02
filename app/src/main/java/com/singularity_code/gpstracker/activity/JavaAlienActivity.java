package com.singularity_code.gpstracker.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.singularity_code.live_location.util.other.AlienPortal;

public class JavaAlienActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startLiveLocationService();
    }

    private final AlienPortal portal = AlienPortal.INSTANCE;
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
                "You can parse anything here to describe your message. "+
                        "For example sender user id, and user name, and anything, " +
                        "in formats that you can decode."
        );

        portal.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        portal.stop();
    }
}
