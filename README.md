# Background Location Service
<p float="left">
<img src="https://github.com/SingularityIndonesia/ForegroundGPSTracker/blob/main/docs/Screenshot%20from%202023-07-07%2005-04-43.png" width="100" alt="Singularity Indonesia">
<img src="https://github.com/SingularityIndonesia/ForegroundGPSTracker/blob/main/docs/Screenshot%20from%202023-07-07%2005-05-03.png" width="100" alt="Singularity Indonesia">
</p>

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
        implementation 'com.github.SingularityIndonesia:ForegroundGPSTracker:1.2.0'
}
```


## Setting Up in Kotlin
See [SetupKotlin.md](docs%2FSetupKotlin.md).

## Setting Up in Java
See [SetupJava.md](docs%2FSetupJava.md).

## Setup Alien
If you are using alien technology such Xamarin/Unity, C++ and other non java platform,
you can create a portal interaction using AlienPortal object.
See [SetupAlien.md](docs%2FSetupAlien.md).

## Networking
This library will automatically sync your data to network, the body request will be
```json
{
  "data": "{\"altitude\":0.0,\"speedAccuracyMeterPerSec\":0.5,\"bearing\":0.0,\"latitude\":37.29547,\"accuracy\":11.256,\"updateTime\":1688681353248,\"speed\":0.0,\"longitude\":-121.1872217,\"verticalAccuracyMeters\":0.5}",
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

## Build AAR Libray
If you need the aar file, to build the library just run the `build-library.sh` in the project root directory.

## Design and Developed by: [Stefanus Ayudha](https://github.com/stefanusayudha)
Feel free to askme any question and support.
## Powered by: [Singularity Indonesia](https://github.com/SingularityIndonesia)

<img src="https://raw.githubusercontent.com/SingularityIndonesia/SingularityIndonesia/main/Logo%20Of%20Singularity%20Indonesia%20%C2%A92023%20Stefanus%20Ayudha.png" width="64" alt="Singularity Indonesia">
