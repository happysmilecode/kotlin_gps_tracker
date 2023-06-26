# Background Location Service

## Dependency Setting

Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
        repositories {
                ...
                maven { url 'https://jitpack.io' }
        }
}
```

Add the dependency
```groovy
dependencies {
        implementation 'com.github.SingularityIndonesia:ForegroundGPSTracker:1.0.0'
}
```


## Setting Up
```kotlin
/** ## First you will need to create service interactor object,
 *  as a proxy for Live Location services **/
private val liveLocationServiceInteractor =
        object : LiveLocationServiceInteractorAbs() {
            
            /** Define Your Context **/
            override val context: Context = this@MainActivity

            /** GPS Configuration Block **/
            override val gpsConfig: GPSConfig =
                object : GPSConfig {
                    override val samplingRate: Long = 10000
                }

            /** Notification Configuration Block **/
            override val notificationConfig: NotificationConfig =
                object : NotificationConfig {
                    override val foregroundServiceID: Int = 1003
                    override val notificationChannelID: String = CHANNEL_ID
                    override val notificationChannelName: String = CHANNEL_NAME
                    override val notificationChannelDescription: String = CHANNEL_DESCRIPTION
                    override val notificationPriority: Int = NotificationCompat.PRIORITY_DEFAULT
                    override val iconRes: Int? = null
                }

            /** Network Configuration Block **/
            override val networkConfiguration: LiveLocationNetworkConfiguration =
                object : LiveLocationNetworkConfiguration {
                    override val url: String = "http://websocket.anakpintarstudio.com?id=terserah_mau_diisi_apa"
                    /** You can define whether using Socket Communication or RESTFUL Api **/ 
                    override val networkMethod: NetworkMethod = NetworkMethod.RESTFULL
                    /** You can put static payload such athorizations and other header what ever you needed **/
                    override val headers: HashMap<String, String> = hashMapOf(
                        "Header1" to "Bearer aasdasdadadadaa",
                        "Header2" to "Bearer 23423094029u40932"
                    )
                    /** Add descriptor to your message **/
                    override val messageDescriptor: String by lazy {
                        val desc = hashMapOf<String, String>(
                            "userID" to userID.toString(),
                            "messagingToken" to messagingToken
                        )

                        Gson().toJson(desc)
                    }
                }

            /** Bellows are the interfaces you can use to get respond in realtime to your application **/
            override fun onServiceStatusChanged(
                serviceStatus: LiveLocationServiceInteractor.ServiceStatus
            ) {
                lifecycleScope.launch {
                    liveLocationRunning.emit(
                        when (serviceStatus) {
                            LiveLocationServiceInteractor.ServiceStatus.RUNNING ->
                                true

                            LiveLocationServiceInteractor.ServiceStatus.DEAD ->
                                false
                        }
                    )
                }
            }

            override fun onError(
                message: String?
            ) {
                lifecycleScope.launch {
                    liveLocationError.emit(
                        message
                    )
                }
            }

            override fun onReceiveUpdate(
                latitude: Double,
                longitude: Double,
                accuracy: Float,
                updateTime: Long
            ) {
                lifecycleScope.launch {
                    location.emit(
                        LatLng(
                            latitude,
                            longitude
                        )
                    )
                }
            }

        }
```

## Networking
This library will automatically sync your data to network, the body request will be
```json
{
  "data": "{\"accuracy\":17.72,\"updateTime\":1687750809224,\"latitude\":-6.8729337,\"longitude\":107.587445}",
  "descriptor": "{\"userID\":\"85158.72113324582\",\"messagingToken\":\"Dummy Messaging Token\"}"
}
```

## Permission
You need to request all these permission in order to make it works:
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```
# Important :
Android not allowing you to request background location before you've granted foreground location.
So the permission request should be done in 2 steps:
- First step is request all foreground permissions
- Second step, after foreground permission granted, you should ask for background permission.

## Note :
- You will need context such activity context or application context both are fine.
- The service will push the data for you, A.k.a automatic network syncronization. All you need are just setting up the network configuration.
- You can use java or kotlin, the syntax won't have a lot different, you can contact me for more help.


## Design and Developed by :
- [Stefanus Ayudha](https://github.com/stefanusayudha)
- [Singularity Indonesia](https://github.com/SingularityIndonesia)

<img src="https://raw.githubusercontent.com/SingularityIndonesia/SingularityIndonesia/main/Logo%20Of%20Singularity%20Indonesia%20%C2%A92023%20Stefanus%20Ayudha.png" width="64" alt="Singularity Indonesia">
