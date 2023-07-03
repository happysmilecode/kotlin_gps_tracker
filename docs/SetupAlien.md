# Setup for Alien
If you are using alien technology such Xamarin/Unity, C++ and other non java platform,
you can use AlienPortal object to interact with the service.

You can see the example here [JavaAlienActivity](..%2Fapp%2Fsrc%2Fmain%2Fjava%2Fcom%2Fsingularity_code%2Fgpstracker%2Factivity%2FJavaAlienActivity.java).

# 1. Get Instance
You can get the instance this way:
```java
// from package com.singularity_code.live_location.util.other.AlienPortal
private final AlienPortal alienPortal = AlienPortal.INSTANCE;
```

# 2. Setup
```java
private void startLiveLocationService() {

    alienPortal.setGPSSamplingRate(1000L);

    // parse your context here, such UnityActivity, Activity, or Application.
    alienPortal.setContext(this);

    alienPortal.setChannelId("Alien Notification");

    alienPortal.setChannelName("Alien Notification");

    alienPortal.setChannelDescription("Notification from alien app");

    alienPortal.setupAPIorSocketURL("http://websocket.company.com?id=" + "userID-" + (Math.random() * 100));

    alienPortal.setNotificationTitle("Live Location");

    alienPortal.setNotificationMessage("Live Location is Running");

    // 0 -> RESTFUL
    // 1 -> Web Socket
    alienPortal.setupNetworkMethod(1);

    // clear previous headers
    alienPortal.clearHeader();
    alienPortal.addHeader("Authentication", "Bearer alsdkmlam");

    alienPortal.setMessageDescriptor(
            "You can parse anything here to describe your message. " +
                    "For example sender user id, and user name, and anything, " +
                    "in formats that you can decode."
    );
}
```

# 3. Start and Stop
```java
alienPortal.start();
alienPortal.stop();
```

# 4. Observing
You will need to update the data manually using thread or something else like thread.
```java
/** # Observer **/
private Thread observerThread;

private boolean stopDebug = false;

private void observeUpdate() {
    observerThread = new Thread() {

        @Override
        public void run() {

            StringBuilder sb;

            while (!stopDebug) {

                String status = alienPortal.getStatus();
                String latitude = alienPortal.getLatitude();
                String longitude = alienPortal.getLongitude();
                String accuracy = alienPortal.getAccuracy();
                String updatedTime = alienPortal.getUpdatedTime();

                sb = new StringBuilder();
                sb.append("status : ").append(status).append("\n");
                sb.append("latitude : ").append(latitude).append("\n");
                sb.append("longitude : ").append(longitude).append("\n");
                sb.append("accuracy : ").append(accuracy).append("\n");
                sb.append("updatedTime : ").append(updatedTime).append("\n");

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
```

# Binding to Unity
You first need to create binder object like so:
```cpp
using UnityEngine;
using System.Collections.Generic;

public class AlienPortalWrapper
{
    private AndroidJavaObject alienPortalInstance;

    public AlienPortalWrapper()
    {
        AndroidJavaClass alienPortalClass = new AndroidJavaClass("com.singularity_code.live_location.util.other.AlienPortal");
        alienPortalInstance = alienPortalClass.CallStatic<AndroidJavaObject>("newInstance");
    }

    public string GetStatus()
    {
        return alienPortalInstance.Call<string>("getStatus");
    }

    public string GetErrorMessage()
    {
        return alienPortalInstance.Call<string>("getErrorMessage");
    }

    public string GetLatitude()
    {
        return alienPortalInstance.Call<string>("getLatitude");
    }

    public string GetLongitude()
    {
        return alienPortalInstance.Call<string>("getLongitude");
    }

    public string GetAccuracy()
    {
        return alienPortalInstance.Call<string>("getAccuracy");
    }

    public string GetUpdatedTime()
    {
        return alienPortalInstance.Call<string>("getUpdatedTime");
    }

    public void SetGPSSamplingRate(long samplingRate)
    {
        alienPortalInstance.Call("setGPSSamplingRate", samplingRate);
    }

    public void SetContext(Context context)
    {
        alienPortalInstance.Call("setContext", context);
    }

    public void SetChannelId(string id)
    {
        alienPortalInstance.Call("setChannelId", id);
    }

    public void SetChannelName(string name)
    {
        alienPortalInstance.Call("setChannelName", name);
    }

    public void SetChannelDescription(string description)
    {
        alienPortalInstance.Call("setChannelDescription", description);
    }

    public void SetupAPIorSocketURL(string url)
    {
        alienPortalInstance.Call("setupAPIorSocketURL", url);
    }

    public void SetNotificationTitle(string title)
    {
        alienPortalInstance.Call("setNotificationTitle", title);
    }

    public void SetNotificationMessage(string message)
    {
        alienPortalInstance.Call("setNotificationMessage", message);
    }

    public void SetupNetworkMethod(int methodEnumIndex)
    {
        alienPortalInstance.Call("setupNetworkMethod", methodEnumIndex);
    }

    public void AddHeader(string key, string value)
    {
        alienPortalInstance.Call("addHeader", key, value);
    }

    public void ClearHeader()
    {
        alienPortalInstance.Call("clearHeader");
    }

    public void SetMessageDescriptor(string descriptor)
    {
        alienPortalInstance.Call("setMessageDescriptor", descriptor);
    }

    public void Start()
    {
        alienPortalInstance.Call("start");
    }

    public void Stop()
    {
        alienPortalInstance.Call("stop");
    }
}
```

Then have you call the function in the exact same orders on the setup section number 2 above before starting the service.