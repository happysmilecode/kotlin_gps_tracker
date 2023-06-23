package com.singularity_code.gpstracker.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.singularity_code.gpstracker.ui.theme.GPSTrackerTheme
import com.singularity_code.gpstracker.util.CHANNEL_DESCRIPTION
import com.singularity_code.gpstracker.util.CHANNEL_ID
import com.singularity_code.gpstracker.util.CHANNEL_NAME
import com.singularity_code.live_location.util.enums.NetworkMethod
import com.singularity_code.live_location.util.pattern.LiveLocationNetworkConfiguration
import com.singularity_code.live_location.util.pattern.LiveLocationServiceInteractor
import com.singularity_code.live_location.util.pattern.LiveLocationServiceInteractorAbs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    /*private val liveLocationServiceBinder: Flow<LiveLocationService.LocalBinder?>
            by getLiveLocationServiceBinder(
                channelID = CHANNEL_ID,
                channelName = CHANNEL_NAME,
                channelDescription = CHANNEL_DESCRIPTION,
                lifecycleOwner = this,
                coroutineScope = lifecycleScope
            )*/

    private val location = MutableStateFlow<LatLng?>(null)
    private val liveLocationRunning = MutableStateFlow(false)
    private val liveLocationError = MutableStateFlow<String?>(null)

    private val liveLocationServiceInteractor =
        object : LiveLocationServiceInteractorAbs() {
            override val context: Context = this@MainActivity
            override val samplingRate: Long = 5000
            override val networkConfiguration: LiveLocationNetworkConfiguration =
                object : LiveLocationNetworkConfiguration {
                    override val url: String = "http://websocket.anakpintarstudio.com?id=terserah_mau_diisi_apa"
                    override val networkMethod: NetworkMethod = NetworkMethod.WEBSOCKET
                    override val headers: HashMap<String, String> = hashMapOf()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE
                ),

                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted
            // Proceed with your desired functionality
        }

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
                            foregroundServiceID = 1003,
                            notificationTitle = "Live Location",
                            notificationMessage = "Singularity Live Location",
                            notificationChannelID = CHANNEL_ID,
                            notificationChannelName = CHANNEL_NAME,
                            notificationChannelDescription = CHANNEL_DESCRIPTION
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