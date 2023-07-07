package com.singularity_code.gpstracker.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.LocationResult;
import com.google.gson.Gson;
import com.singularity_code.live_location.util.enums.NetworkMethod;
import com.singularity_code.live_location.util.pattern.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.singularity_code.gpstracker.util.ConstKt.*;

public class JavaActivityExample extends Activity {
    private String userID = "USER ID";
    private String messagingToken = "Dummy Messaging Token";

    private LiveLocationServiceInteractor liveLocationServiceInteractor = new LiveLocationServiceInteractorAbs() {
        @Override
        public Context getContext() {
            return JavaActivityExample.this;
        }

        @Override
        public GPSConfig getGpsConfig() {
            return new GPSConfig() {

                @Override
                public long getSamplingRate() {
                    return 10000;
                }
            };
        }

        @Override
        public NotificationConfig getNotificationConfig() {
            return new NotificationConfig() {
                @Override
                public int getForegroundServiceID() {
                    return 1003;
                }

                @Override
                public String getNotificationChannelID() {
                    return CHANNEL_ID;
                }

                @Override
                public String getNotificationChannelName() {
                    return CHANNEL_NAME;
                }

                @Override
                public String getNotificationChannelDescription() {
                    return CHANNEL_DESCRIPTION;
                }

                @Override
                public int getNotificationPriority() {
                    return NotificationCompat.PRIORITY_DEFAULT;
                }

                @Override
                public Integer getIconRes() {
                    return null;
                }
            };
        }

        @Override
        public LiveLocationNetworkConfiguration getNetworkConfiguration() {
            return new LiveLocationNetworkConfiguration() {
                @Override
                public String getUrl() {
                    return "http://websocket.anakpintarstudio.com?id=" + userID;
                }

                @Override
                public NetworkMethod getNetworkMethod() {
                    return NetworkMethod.WEBSOCKET;
                }

                @Override
                public HashMap<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Header1", "Bearer aasdasdadadadaa");
                    headers.put("Header2", "Bearer 23423094029u40932");
                    return headers;
                }

                @Override
                public String getMessageDescriptor() {
                    HashMap<String, String> desc = new HashMap<>();
                    desc.put("userID", String.valueOf(userID));
                    desc.put("messagingToken", messagingToken);
                    return new Gson().toJson(desc);
                }
            };
        }

        @Override
        public void onServiceStatusChanged(LiveLocationServiceInteractor.ServiceStatus serviceStatus) {
            // TODO : Do what you need
        }

        @Override
        public void onError(String message) {
            // TODO : Do what you need
        }

        @Override
        public void onReceiveUpdate(@NotNull LocationResult location) {
            // TODO : Do what you need
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        liveLocationServiceInteractor.startService(
                "Live Location",
                "Singularity Live Location"
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        liveLocationServiceInteractor.stopService();
    }
}
