package com.singularity_code.live_location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.singularity_code.live_location.data.Repository
import com.singularity_code.live_location.data.RestfulRepository
import com.singularity_code.live_location.data.WebSocketRepository
import com.singularity_code.live_location.util.ERROR_MISSING_LOCATION_PERMISSION
import com.singularity_code.live_location.util.ErrorMessage
import com.singularity_code.live_location.util.enums.NetworkMethod
import com.singularity_code.live_location.util.isGPSEnabled
import com.singularity_code.live_location.util.pattern.LiveLocationNetworkConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File

class LiveLocationService : Service() {

    companion object {
        fun createIntent(
            context: Context,
            foregroundServiceID: Int,
            notificationChannelID: String,
            notificationChannelName: String,
            notificationChannelDescription: String,
            notificationTitle: String,
            notificationMessage: String,
            gpsSamplingRate: Long,
            networkConfiguration: LiveLocationNetworkConfiguration,
            notificationPriority: Int,
            @DrawableRes iconRes: Int?,
            messageDescriptor: String
        ): Intent {
            return Intent(
                context,
                LiveLocationService::class.java
            ).apply {
                putExtra("foregroundServiceID", foregroundServiceID)
                putExtra("notificationChannelID", notificationChannelID)
                putExtra("notificationChannelName", notificationChannelName)
                putExtra("notificationChannelDescription", notificationChannelDescription)
                putExtra("notificationTitle", notificationTitle)
                putExtra("notificationMessage", notificationMessage)
                putExtra("gpsSamplingRate", gpsSamplingRate)
                putExtra("notificationPriority", notificationPriority)
                putExtra("url", networkConfiguration.url)
                putExtra("headers", Gson().toJson(networkConfiguration.headers))
                putExtra("networkMethod", networkConfiguration.networkMethod.ordinal)
                putExtra("iconRes", iconRes ?: R.drawable.ic_share_location)
                putExtra("messageDescriptor", messageDescriptor)
            }
        }
    }

    data class LocationData(
        val locationResult: LocationResult,
        val updateTime: Long
    )

    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    private val liveLocationError = MutableStateFlow<ErrorMessage?>(null)
    private val currentLocation = MutableStateFlow<LocationData?>(null)
    private val liveLocationRunning = MutableStateFlow(false)
    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(gpsSamplingRate)
            .setFastestInterval(gpsSamplingRate)
    }

    private val locationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this.applicationContext)
    }

    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            p0.runCatching {
                if (lastLocation == null)
                    throw Error("location result null")

                coroutineScope.launch {
                    val updateTime = System.currentTimeMillis()

                    val data = hashMapOf(
                        "latitude" to lastLocation!!.latitude,
                        "longitude" to lastLocation!!.longitude,
                        "accuracy" to lastLocation!!.accuracy,
                        "altitude" to lastLocation!!.altitude,
                        "bearing" to lastLocation!!.bearing,
                        "speed" to lastLocation!!.speed,
                        "updateTime" to updateTime
                    ).apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            put(
                                "speedAccuracyMeterPerSec", lastLocation!!.speedAccuracyMetersPerSecond,
                            )
                            put(
                                "verticalAccuracyMeters", lastLocation!!.verticalAccuracyMeters
                            )
                        }
                    }.let {
                        Gson().toJson(it)
                    }.also {
                        Log.d(this@LiveLocationService::class.simpleName, "new location: $it")
                    }

                    val payload = hashMapOf<String, String>()
                        .apply {
                            if (messageDescriptor != null && messageDescriptor?.isNotBlank() == true) {
                                put("descriptor", messageDescriptor!!)
                            }

                            put("data", data)
                        }
                        .let {
                            Gson().toJson(it)
                        }

                    Log.d(this@LiveLocationService::class.simpleName, "push data to server: $payload")
                    repository.sendData(payload)
                        .mapLeft {
                            liveLocationError.emit(it)
                        }
                        .map {
                            liveLocationError.emit(null)
                        }

                    currentLocation.emit(
                        LocationData(
                            locationResult = p0,
                            updateTime
                        )
                    )

                    liveLocationError.emit(null)
                }

            }.onFailure {

                coroutineScope.launch {
                    liveLocationError.emit(
                        it.message ?: it.cause?.message ?: "Unknown Error"
                    )
                }
            }
        }
    }

    private lateinit var repository: Repository
    private var foregroundServiceID: Int? = null
    private lateinit var notificationChannelID: String
    private lateinit var notificationChannelName: String
    private lateinit var notificationChannelDescription: String
    private lateinit var notificationTitle: String
    private lateinit var notificationMessage: String
    private var notificationPriority: Int = NotificationCompat.PRIORITY_DEFAULT
    private lateinit var url: String
    private lateinit var headers: HashMap<String, String>
    private lateinit var networkMethod: NetworkMethod
    private var gpsSamplingRate: Long = 5000
    private var messageDescriptor: String? = null

    @DrawableRes
    private var iconRes: Int? = null
    private var lastIntent: Intent? = null

    private fun startLocationService(
        intent: Intent?
    ) {
        lastIntent = intent
        foregroundServiceID = intent?.getIntExtra("foregroundServiceID", 1005) ?: 1005
        notificationChannelID = intent?.getStringExtra("notificationChannelID") ?: "notificationChannelID"
        notificationChannelName = intent?.getStringExtra("notificationChannelName") ?: "notificationChannelName"
        notificationChannelDescription =
            intent?.getStringExtra("notificationChannelDescription") ?: "notificationChannelDescription"
        notificationTitle = intent?.getStringExtra("notificationTitle") ?: "notificationTitle"
        notificationMessage = intent?.getStringExtra("notificationMessage") ?: "notificationMessage"
        notificationPriority = intent?.getIntExtra("notificationPriority", NotificationCompat.PRIORITY_DEFAULT)
            ?: NotificationCompat.PRIORITY_DEFAULT
        iconRes = intent?.getIntExtra("iconResource", R.drawable.ic_share_location)
            ?: R.drawable.ic_share_location
        gpsSamplingRate = intent?.getLongExtra("gpsSamplingRate", 5000L) ?: 5000L
        url = intent?.getStringExtra("url") ?: ""
        headers = (intent?.getStringExtra("headers") ?: "")
            .let {
                val gson = Gson()
                val type = object : TypeToken<HashMap<String, String>>() {}.type
                gson.fromJson(it, type)
            }
        networkMethod = (intent?.getIntExtra("networkMethod", 0) ?: 0)
            .let { ordinal -> NetworkMethod.values()[ordinal] }

        messageDescriptor = intent?.getStringExtra("messageDescriptor")

        coroutineScope.launch {

            /** permission check **/
            run {
                if (ActivityCompat.checkSelfPermission(
                        this@LiveLocationService,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@LiveLocationService,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    coroutineScope.launch {
                        liveLocationRunning.emit(false)
                        liveLocationError.emit(ERROR_MISSING_LOCATION_PERMISSION)
                    }
                    return@launch
                }
            }

            /** notification **/
            run {
                createNotificationChannel()

                val builder = NotificationCompat.Builder(
                    this@LiveLocationService,
                    notificationChannelID
                )

                val notification = builder
                    .apply {
                        setContentTitle(notificationTitle)
                        setContentText(notificationMessage)
                        priority = notificationPriority
                        if (iconRes != null) {
                            setSmallIcon(iconRes!!)
                        }
                        setSound(null)
                    }
                    .build()

                startForeground(
                    foregroundServiceID ?: 1005,
                    notification
                )
            }

            /** prepare websocket connection **/
            run {
                repository = when (networkMethod) {
                    NetworkMethod.WEBSOCKET -> WebSocketRepository(
                        url, headers, this@LiveLocationService
                    )

                    else -> RestfulRepository(
                        url, headers, this@LiveLocationService
                    )
                }
                repository.openConnection()
            }

            /** start location watcher **/
            run {
                locationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallBack,
                    Looper.getMainLooper()
                )
            }

            liveLocationRunning.emit(true)
            liveLocationError.emit(null)
        }

    }

    private fun stopLocationService() {
        /** close web socket **/
        run {
            repository.closeConnection()
        }

        /** destroy notification **/
        run {
            if (foregroundServiceID != null) {
                stopForeground(foregroundServiceID!!)
                foregroundServiceID = null
            }
            removeNotificationChannel()
        }

        /** stop location watcher **/
        runCatching {
            locationProviderClient.removeLocationUpdates(locationCallBack)
        }

        coroutineScope.launch {
            liveLocationRunning.emit(false)
        }

        return
    }

    /**
     * Binding service
     */
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        val liveLocationError: Flow<ErrorMessage?> = this@LiveLocationService.liveLocationError
        val currentLocation: Flow<LocationData?> = this@LiveLocationService.currentLocation
        val liveLocationRunning = this@LiveLocationService.liveLocationRunning
        val isGPSEnabled: Boolean get() = this@LiveLocationService.isGPSEnabled

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create or update the notification channel
            val notificationChannel = NotificationChannel(
                notificationChannelID,
                notificationChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = notificationChannelDescription
            notificationChannel.setShowBadge(true)
            /*notificationChannel.lockscreenVisibility = NotificationChannel.VISIBILITY_PUBLIC*/

            // Set the custom icon for the notification channel
            notificationChannel.setSound(
                Uri.fromFile(File("//assets/no_sound_short.mp3")), null
            )
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationChannel.vibrationPattern = LongArray(0)
            notificationChannel.enableVibration(false)

            // Set the desired icon for the notification channel
            /*notificationChannel.ico(Icon.createWithResource(context, R.drawable.custom_notification_icon))*/

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun removeNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The id of the channel.
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(notificationChannelID)
        }
    }

    override fun onBind(
        intent: Intent?
    ): IBinder {
        startLocationService(intent)
        return binder
    }

    override fun onUnbind(
        intent: Intent?
    ): Boolean {
        runCatching {
            stopLocationService()
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            stopLocationService()
        }
    }
}