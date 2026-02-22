package com.example.app.feature.login.ui

import Routes
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.feature.components.HeadingTextComponent
import com.example.app.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val viewModel: LoginViewModel = hiltViewModel()
    val otpState by viewModel.otpRequestState.collectAsState()

    var mobileNumber by remember { mutableStateOf("") }
    var mobileError by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    // For shake animation
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    fun triggerError() {
        mobileError = true
        coroutineScope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    -12f at 50
                    12f at 100
                    -8f at 150
                    8f at 200
                    -4f at 250
                    4f at 300
                    0f at 400
                }
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            HeadingTextComponent(heading = "Login")
            Spacer(modifier = Modifier.height(40.dp))

            MyTextField(
                value = mobileNumber,
                onValueChange = {
                    val digits = it.filter(Char::isDigit)
                    mobileNumber = digits.takeLast(10)

                    if (mobileNumber.length == 10) {
                        keyboardController?.hide()
                    }

                    if (mobileError) mobileError = false
                },
                labelVal = "Mobile number",
                icon = R.drawable.ic_lockphone,
                isError = mobileError,
                shakeOffset = shakeOffset.value,
                onDone = {
                    if (mobileNumber.length == 10) {
                        keyboardController?.hide()   // ✅ THIS WAS MISSING
                    } else {
                        triggerError()
                    }
                }
            )

            if (mobileError) {
                Text(
                    text = "Please enter a valid 10-digit mobile number",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                SendOtpButton(
                    mobile = mobileNumber,
                    isLoading = otpState is LoginUiState.Loading
                ) {
                    if (mobileNumber.isEmpty()) {
                        mobileError = true
                    } else if (mobileNumber.length == 10) {
                        viewModel.requestOtp(mobileNumber)
                    } else {
                        triggerError()
                    }
                }
                LaunchedEffect(otpState) {
                    when (otpState) {
                        is LoginUiState.Success -> {
                            navController.navigate(Routes.Screen.Auth.otpRoute(mobileNumber))
                            // Reset state to prevent re-navigation when coming back
                            viewModel.resetState()
                        }

                        is LoginUiState.Error -> {
                            val msg = (otpState as LoginUiState.Error).message
                            // Optional: show Toast if you want
                            // Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }

            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelVal: String,
    icon: Int,
    isError: Boolean,
    shakeOffset: Float,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = shakeOffset.dp),
        isError = isError,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = if (isError) Color.Red else Color.White,
            unfocusedIndicatorColor = if (isError) Color.Red else Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent
        ),
        placeholder = {
            Text(text = labelVal, color = Color.Gray)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color.White
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        singleLine = true
    )
}


@Composable
fun SendOtpButton(
    mobile: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Send OTP",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}