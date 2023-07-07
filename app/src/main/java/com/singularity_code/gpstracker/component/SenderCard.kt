package com.singularity_code.gpstracker.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.singularity_code.live_location.util.pattern.LiveLocationServiceInteractor

@Composable
fun SenderCard(
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