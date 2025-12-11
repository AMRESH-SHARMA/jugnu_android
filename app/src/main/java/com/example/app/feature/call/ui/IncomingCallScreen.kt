package com.example.app.feature.call.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.navigation.ui.Routes
import com.example.app.feature.navigation.ui.openOngoingCall
import kotlinx.coroutines.launch

@Composable
fun IncomingCallScreen(
    listener: ListenerModel,
    navController: NavController,
) {
    val vm: CallViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    // 🔥 LISTEN FOR BACKEND EVENTS (call_rejected, call_cancelled, call_ended)
    LaunchedEffect(Unit) {
        CallEventBus.events.collect { event ->
            if (event is CallEvent.CallRejected || event is CallEvent.CallEnded) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.CALL_ROOT) { inclusive = true }
                }
            }
        }
    }


    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AsyncImage(
                model = listener.avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = listener.name,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Incoming voice call",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // DECLINE
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        vm.rejectCall()
                        navController.popBackStack()
                    }
                },
                containerColor = Color.Red,
                shape = CircleShape,
                modifier = Modifier.size(70.dp)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null)
            }

            Spacer(Modifier.width(60.dp))

            // ACCEPT
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        vm.acceptCall()
                        navController.openOngoingCall(listener)
                    }
                },
                containerColor = Color.Green,
                shape = CircleShape,
                modifier = Modifier.size(70.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
            }
        }
    }
}
