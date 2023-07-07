package com.singularity_code.live_location.util.other;

import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.LocationResult;
import com.singularity_code.live_location.util.enums.NetworkMethod;
import com.singularity_code.live_location.util.pattern.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class AlienPortal {

    public static AlienPortal newInstance() {
        return new AlienPortal();
    }

    // observable
    private String _status = "DEAD";

    public String getStatus() {
        return _status;
    }

    private String _errorMessage;

    public String getErrorMessage() {
        return _errorMessage;
    }

    private String _latitude;

    public String getLatitude() {
        return _latitude;
    }

    private String _altitude;

    public String getAltitude() {
        return _altitude;
    }

    private String _altitudeAccuracyMeter;
    public String getAltitudeAccuracyMeter() {
        return _altitudeAccuracyMeter;
    }
    private String _longitude;

    public String getLongitude() {
        return _longitude;
    }

    private String _accuracy;

    public String getAccuracy() {
        return _accuracy;
    }

    private String _updatedTime;

    public String getUpdatedTime() {
        return _updatedTime;
    }

    // gps config
    private long gpsSamplingRate = 10000L;

    // notification config
    private Context context;
    private String channelId;
    private String channelName;
    private String channelDescription;
    private String notificationTitle;
    private String notificationMessage;

    // network configuration
    private String apiOrSocketURL;
    private NetworkMethod networkMethod;
    private String messageDescriptor;
    private HashMap<String, String> headers = new HashMap<>();

    private LiveLocationServiceInteractor liveLocationServiceInteractor;

    public void setGPSSamplingRate(long samplingRate) {
        gpsSamplingRate = samplingRate;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setChannelId(String id) {
        channelId = id;
    }

    public void setChannelName(String name) {
        channelName = name;
    }

    public void setChannelDescription(String description) {
        channelDescription = description;
    }

    public void setupAPIorSocketURL(String url) {
        apiOrSocketURL = url;
    }

    public void setNotificationTitle(String title) {
        notificationTitle = title;
    }

    public void setNotificationMessage(String message) {
        notificationMessage = message;
    }

    // 0 -> RESTFUL
    // 1 -> Web Socket
    public void setupNetworkMethod(int methodEnumIndex) {
        networkMethod = NetworkMethod.values()[methodEnumIndex];
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void clearHeader() {
        headers.clear();
    }

    public void setMessageDescriptor(String descriptor) {
        messageDescriptor = descriptor;
    }

    public void start() {
        liveLocationServiceInteractor = new LiveLocationServiceInteractorAbs() {
            @Override
            public Context getContext() {
                return AlienPortal.this.context;
            }

            @NotNull
            @Override
            public GPSConfig getGpsConfig() {
                return new GPSConfig() {
                    @Override
                    public long getSamplingRate() {
                        return gpsSamplingRate;
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
                        return AlienPortal.this.channelId;
                    }

                    @Override
                    public String getNotificationChannelName() {
                        return AlienPortal.this.channelName;
                    }

                    @Override
                    public String getNotificationChannelDescription() {
                        return AlienPortal.this.channelDescription;
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
                        return AlienPortal.this.apiOrSocketURL;
                    }

                    @Override
                    public NetworkMethod getNetworkMethod() {
                        return AlienPortal.this.networkMethod;
                    }

                    @Override
                    public HashMap<String, String> getHeaders() {
                        return AlienPortal.this.headers;
                    }

                    @Override
                    public String getMessageDescriptor() {
                        return AlienPortal.this.messageDescriptor;
                    }
                };
            }

            @Override
            public void onServiceStatusChanged(LiveLocationServiceInteractor.ServiceStatus serviceStatus) {
                AlienPortal.this._status = serviceStatus.name();
            }

            @Override
            public void onError(String message) {
                AlienPortal.this._errorMessage = message;
            }

            @Override
            public void onReceiveUpdate(@NotNull LocationResult location) {
                if (location.getLastLocation() == null) return;

                AlienPortal.this._latitude = String.valueOf(location.getLastLocation().getLatitude());
                AlienPortal.this._longitude = String.valueOf(location.getLastLocation().getLongitude());
                AlienPortal.this._accuracy = String.valueOf(location.getLastLocation().getAccuracy());
                AlienPortal.this._updatedTime = String.valueOf(location.getLastLocation().getTime());

                if (location.getLastLocation().hasAltitude()) {
                    AlienPortal.this._altitude = String.valueOf(location.getLastLocation().getAltitude());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        AlienPortal.this._altitudeAccuracyMeter = String.valueOf(location.getLastLocation().getVerticalAccuracyMeters());
                    }
                }
            }
        };

        liveLocationServiceInteractor.startService(
                AlienPortal.this.notificationTitle,
                AlienPortal.this.notificationMessage
        );
    }

    public void stop() {
        if (liveLocationServiceInteractor != null) {
            liveLocationServiceInteractor.stopService();
            liveLocationServiceInteractor = null;
        }
    }
}