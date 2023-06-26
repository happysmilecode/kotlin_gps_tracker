package com.singularity_code.gpstracker.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.singularity_code.gpstracker.ui.theme.GPSTrackerTheme
import com.singularity_code.gpstracker.util.CHANNEL_DESCRIPTION
import com.singularity_code.gpstracker.util.CHANNEL_ID
import com.singularity_code.gpstracker.util.CHANNEL_NAME
import com.singularity_code.live_location.util.enums.NetworkMethod
import com.singularity_code.live_location.util.pattern.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE = 100
        const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    private val userID by lazy { Math.random() * 100000 }
    private val messagingToken by lazy { "Dummy Messaging Token" }

    private val location = MutableStateFlow<LatLng?>(null)
    private val liveLocationRunning = MutableStateFlow(false)
    private val liveLocationError = MutableStateFlow<String?>(null)

    private val liveLocationServiceInteractor =
        object : LiveLocationServiceInteractorAbs() {
            override val context: Context = this@MainActivity

            override val gpsConfig: GPSConfig =
                object : GPSConfig {
                    override val samplingRate: Long = 10000
                }

            override val notificationConfig: NotificationConfig =
                object : NotificationConfig {
                    override val foregroundServiceID: Int = 1003
                    override val notificationChannelID: String = CHANNEL_ID
                    override val notificationChannelName: String = CHANNEL_NAME
                    override val notificationChannelDescription: String = CHANNEL_DESCRIPTION
                    override val notificationPriority: Int = NotificationCompat.PRIORITY_DEFAULT
                    override val iconRes: Int? = null
                }

            override val networkConfiguration: LiveLocationNetworkConfiguration =
                object : LiveLocationNetworkConfiguration {
                    override val url: String = "http://websocket.company.com?id=${Math.random() * 1000}"
                    override val networkMethod: NetworkMethod = NetworkMethod.WEBSOCKET
                    override val headers: HashMap<String, String> = hashMapOf(
                        "Header1" to "Bearer aasdasdadadadaa",
                        "Header2" to "Bearer 23423094029u40932"
                    )
                    override val messageDescriptor: String by lazy {
                        val desc = hashMapOf<String, String>(
                            "userID" to userID.toString(),
                            "messagingToken" to messagingToken
                        )

                        Gson().toJson(desc)
                    }
                }

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


    private fun checkPermission(
        permissions: List<String>
    ): Boolean {
        return permissions.map { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }.fold(true) { l, r ->
            l && r
        }
    }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Request Permission **/
        requestForegroundPermission()

        setContent {
            GPSTrackerTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Text(
                            text = "Sender",
                            modifier = Modifier.padding(16.dp, 16.dp, 0.dp, 0.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Sender(
                            liveLocationServiceInteractor,
                            location.collectAsState().value,
                            liveLocationRunning.collectAsState().value,
                            liveLocationError.collectAsState().value
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    private fun requestForegroundPermission() {
        val hasPermission = checkPermission(
            listOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        if (!hasPermission) {
            // Permission is not granted
            // Request the permission
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE
                ),

                FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("InlinedApi")
    private fun requestBackgroundPermission() {
        if (!checkPermission(
                listOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),

                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this,
                "Location tracker wont work, please re open application and grant all permissions",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        when (requestCode) {
            FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                /** request background permission **/
                requestBackgroundPermission()
            }
        }
    }
}

@Composable
fun Sender(
    interactor: LiveLocationServiceInteractor,
    location: LatLng?,
    liveLocationRunning: Boolean,
    liveLocationError: String?
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = location.toString())
            Text(text = "Running $liveLocationRunning")
            Text(text = "Error $liveLocationError")
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = {
                        interactor.stopService()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = liveLocationRunning ?: false
                ) {
                    Text(text = "Stop Service")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        interactor.startService(
                            notificationTitle = "Live Location",
                            notificationMessage = "Singularity Live Location"
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !(liveLocationRunning ?: false)
                ) {
                    Text(text = "Start Service")
                }
            }

        }
    }
}

@Composable
fun Receiver() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Location = ")
            Text(text = "Updated Time = ${System.currentTimeMillis()}")
        }
    }
}