package com.example.app.feature.call.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app.feature.call.domain.CallModel
import com.example.app.feature.call.domain.CallStatus

@Composable
fun IncomingCallScreen(
    vm: CallViewModel
) {

    Log.d("RTM", "Inside incoming call")

    // 🔥 Observe call state only
    val call by vm.callModel.collectAsState()

    // Safety: screen may briefly exist while state changes
    if (call == null || call!!.status != CallStatus.INCOMING_RINGING) {
        return
    }

    IncomingCallContent(
        call = call!!,
        onAccept = vm::acceptCall,
        onReject = vm::rejectCall
    )
}

// ----------------------------------------------------------------------
// CONTENT
// ----------------------------------------------------------------------

@Composable
private fun IncomingCallContent(
    call: CallModel,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ---------------- CALL INFO ----------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Incoming Call",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Caller ID: ${call.callerAccountId}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ---------------- ACTION BUTTONS ----------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // REJECT
            FloatingActionButton(
                onClick = onReject,
                containerColor = Color.Red
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Reject Call",
                    tint = Color.White
                )
            }

            // ACCEPT
            FloatingActionButton(
                onClick = onAccept,
                containerColor = Color(0xFF2E7D32) // green
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Accept Call",
                    tint = Color.White
                )
            }
        }
    }
}
