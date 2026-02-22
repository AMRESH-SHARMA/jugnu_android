package com.example.app.feature.login.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app.feature.components.HeadingTextComponent
import com.example.app.R


private const val OTP_LENGTH = 6

@Composable
fun OtpVerificationScreen(
    navController: NavController,
    mobile: String
) {
    val viewModel: OtpViewModel = hiltViewModel()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var otp by remember { mutableStateOf("") }
    var showValidationError by remember { mutableStateOf(false) }
    val isOtpComplete = otp.length == OTP_LENGTH

    val maskedMobile = remember(mobile) {
        "*******${mobile.takeLast(2)}"
    }

    val otpVerifyState by viewModel.otpVerifyState.collectAsState()
    val resendTime by viewModel.resendTimer.collectAsState()

    // ---------------- Handle OTP verification result ----------------
    LaunchedEffect(otpVerifyState) {
        when (otpVerifyState) {
            is OtpUiState.Success -> {
                val data = (otpVerifyState as OtpUiState.Success).data as? com.example.app.feature.login.domain.VerifyOtpResult
                
                Toast.makeText(context, "✓ Login successful!", Toast.LENGTH_SHORT).show()
                
                // Always navigate to PermissionScreen first
                // PermissionScreen will handle routing to ProfileSetup or Home/Dashboard
                navController.navigate(Routes.Screen.Auth.PERMISSION) {
                    popUpTo(Routes.Screen.Auth.LOGIN) { inclusive = false }
                    launchSingleTop = true
                }
            }

            is OtpUiState.Error -> {
                val msg = (otpVerifyState as OtpUiState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }

    // ------------------ UI ------------------
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Verification Code",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Enter the 6-digit code sent to",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = maskedMobile,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // OTP Input
                OtpInput(
                    otp = otp,
                    onOtpChange = {
                        otp = it
                        viewModel.clearOtpError()
                        if (showValidationError) showValidationError = false
                        if (it.length == OTP_LENGTH) keyboardController?.hide()
                    },
                    focusRequester = focusRequester
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Validation Error (empty OTP)
                if (showValidationError) {
                    Text(
                        text = "Please enter the complete 6-digit OTP",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Error Message (from API)
                OtpErrorMessage(
                    otpUiState = otpVerifyState
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Resend Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Didn't receive code?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        if (resendTime > 0) {
                            Text(
                                text = "Resend in ${resendTime}s",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        } else {
                            TextButton(
                                onClick = { viewModel.resendOtp(mobile) },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    "Resend",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        onClick = {
                            keyboardController?.hide()
                            navController.navigate(Routes.Screen.Auth.LOGIN) {
                                popUpTo(Routes.Screen.Auth.LOGIN) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = "Back",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        )
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = otpVerifyState !is OtpUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            if (isOtpComplete) {
                                viewModel.verifyOtp(mobile, otp)
                            } else {
                                showValidationError = true
                            }
                        }
                    ) {
                        if (otpVerifyState is OtpUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Verify",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
private fun OtpErrorMessage(
    otpUiState: OtpUiState
) {
    if (otpUiState !is OtpUiState.Error) return

    val message = when {
        otpUiState.message.contains("invalid", ignoreCase = true) ->
            "Incorrect OTP. Please try again."

        otpUiState.message.contains("expired", ignoreCase = true) ->
            "OTP has expired. Please request a new one."

        otpUiState.message.contains("locked", ignoreCase = true) ->
            "Too many attempts. OTP is locked. Try again later."

        else -> otpUiState.message
    }

    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 4.dp)
    )
}

/* ------------------ OTP INPUT ------------------ */
@Composable
private fun OtpInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(contentAlignment = Alignment.Center) {

        // 👇 Auto hide keyboard when OTP becomes complete (handles autofill)
        LaunchedEffect(otp) {
            if (otp.length == OTP_LENGTH) {
                keyboardController?.hide()
            }
        }

        // Hidden textfield for input
        BasicTextField(
            value = otp,
            onValueChange = { value ->
                val digits = value.filter(Char::isDigit)

                if (digits.length <= OTP_LENGTH) {
                    onOtpChange(digits)

                    // 👇 Hide keyboard immediately when autofill/paste completes OTP
                    if (digits.length == OTP_LENGTH) {
                        keyboardController?.hide()
                    }
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp)
                .alpha(0f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            )
        )

        // Responsive row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val boxSize =
                ((LocalContext.current.resources.displayMetrics.widthPixels /
                        LocalContext.current.resources.displayMetrics.density)
                        - 48 - 8 * (OTP_LENGTH - 1)) / OTP_LENGTH

            repeat(OTP_LENGTH) { index ->
                AnimatedOtpBox(
                    value = otp.getOrNull(index)?.toString() ?: "",
                    isFocused = otp.length == index,
                    size = boxSize.dp
                )
            }
        }
    }
}
@Composable
private fun AnimatedOtpBox(value: String, isFocused: Boolean, size: Dp) {
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 250)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (value.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else Color.Transparent,
        animationSpec = tween(durationMillis = 250)
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .shadow(elevation = if (isFocused) 8.dp else 2.dp, shape = RoundedCornerShape(12.dp))
            .animateContentSize(animationSpec = tween(250)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge
        )
    }
}