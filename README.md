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
        implementation 'com.github.SingularityIndonesia:ForegroundGPSTracker:1.1.0'
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

## Build AAR Libray
If you need the aar file, to build the library just run the `build-library.sh` in the project root directory.

## Design and Developed by :
- [Stefanus Ayudha](https://github.com/stefanusayudha)
- [Singularity Indonesia](https://github.com/SingularityIndonesia)

<img src="https://raw.githubusercontent.com/SingularityIndonesia/SingularityIndonesia/main/Logo%20Of%20Singularity%20Indonesia%20%C2%A92023%20Stefanus%20Ayudha.png" width="64" alt="Singularity Indonesia">
