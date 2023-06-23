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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.singularity_code.live_location.data.Repository
import com.singularity_code.live_location.util.ERROR_MISSING_LOCATION_PERMISSION
import com.singularity_code.live_location.util.ErrorMessage
import com.singularity_code.live_location.util.isGPSEnabled
import com.singularity_code.live_location.util.websocket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.WebSocket
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
            notificationPriority: Int
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
                putExtra("notificationPriority", notificationPriority)
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
    private val locationRequest: LocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(5000)
        .setFastestInterval(5000)

    private val locationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this.applicationContext)
    }

    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            p0.runCatching {
                coroutineScope.launch {
                    currentLocation.emit(
                        LocationData(
                            locationResult = p0,
                            updateTime = System.currentTimeMillis()
                        )
                    )

                    webSocket.send("Live Location LatLng: ${lastLocation.latitude}, ${lastLocation.longitude}")
                    liveLocationError.emit(null)
                }

                Log.d("TAG", "onLocationResult: ${p0.lastLocation}")
            }.onFailure {

                coroutineScope.launch {
                    liveLocationError.emit(
                        it.message ?: it.cause?.message ?: "Unknown Error"
                    )
                }
                Log.d("TAG", "onLocationResult: $it")
            }
        }
    }

    private lateinit var webSocket: WebSocket
    private var foregroundServiceID: Int? = null
    private lateinit var notificationChannelID: String
    private lateinit var notificationChannelName: String
    private lateinit var notificationChannelDescription: String
    private lateinit var notificationTitle: String
    private lateinit var notificationMessage: String
    private var notificationPriority: Int = NotificationCompat.PRIORITY_DEFAULT

    private fun startLocationService(
        intent: Intent?
    ) {
        foregroundServiceID = intent?.getIntExtra("foregroundServiceID", 1005) ?: 1005
        notificationChannelID = intent?.getStringExtra("notificationChannelID") ?: "notificationChannelID"
        notificationChannelName = intent?.getStringExtra("notificationChannelName") ?: "notificationChannelName"
        notificationChannelDescription = intent?.getStringExtra("notificationChannelDescription") ?: "notificationChannelDescription"
        notificationTitle = intent?.getStringExtra("notificationTitle") ?: "notificationTitle"
        notificationMessage = intent?.getStringExtra("notificationMessage") ?: "notificationMessage"
        notificationPriority = intent?.getIntExtra("notificationPriority", NotificationCompat.PRIORITY_DEFAULT)
            ?: NotificationCompat.PRIORITY_DEFAULT

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
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationMessage)
                    .setSmallIcon(androidx.appcompat.R.drawable.abc_ic_clear_material)
                    .setPriority(notificationPriority)
                    .setSound(null)
                    .build()

                startForeground(
                    foregroundServiceID ?: 1005,
                    notification
                )
            }

            /** prepare websocket connection **/
            run {
                webSocket = websocket()
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

        /** close web socker **/
        run {
            webSocket.close(1000, "service stop")
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
        /** kill this service **/
        stopSelf()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            stopLocationService()
        }
    }
}