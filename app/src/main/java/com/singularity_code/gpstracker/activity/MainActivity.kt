package com.singularity_code.gpstracker.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.singularity_code.gpstracker.ui.theme.GPSTrackerTheme
import com.singularity_code.gpstracker.util.CHANNEL_DESCRIPTION
import com.singularity_code.gpstracker.util.CHANNEL_ID
import com.singularity_code.gpstracker.util.CHANNEL_NAME
import com.singularity_code.live_location.LiveLocationService
import com.singularity_code.live_location.util.getLiveLocationServiceBinder
import kotlinx.coroutines.flow.Flow

class MainActivity : ComponentActivity() {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    private val liveLocationServiceBinder: Flow<LiveLocationService.LocalBinder?>
            by getLiveLocationServiceBinder(
                channelID = CHANNEL_ID,
                channelName = CHANNEL_NAME,
                channelDescription = CHANNEL_DESCRIPTION,
                lifecycleOwner = this,
                coroutineScope = lifecycleScope
            )

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
                        Sender(liveLocationServiceBinder)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun Sender(
    liveLocationServiceBinder: Flow<LiveLocationService.LocalBinder?>
) {
    val binder = liveLocationServiceBinder.collectAsState(initial = null).value
    val location = binder?.currentLocation?.collectAsState(null)?.value
    val liveLocationRunning = binder?.liveLocationRunning?.collectAsState(false)?.value
    val liveLocationError = binder?.liveLocationError?.collectAsState("")?.value

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
                        binder?.stopService()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = liveLocationRunning ?: false
                ) {
                    Text(text = "Stop Service")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        binder?.startService(
                            serviceID = 1003,
                            channelID = CHANNEL_ID,
                            serviceTitle = "Live Location",
                            notificationMessage = "Singularity Live Location",
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