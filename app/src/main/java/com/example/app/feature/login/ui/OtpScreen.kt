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
import com.example.app.feature.components.ImageComponent
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
                Toast.makeText(context, "OTP Verified!", Toast.LENGTH_SHORT).show()
                // Navigate to next screen
                // navController.navigate(Routes.Screen.Home)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            ImageComponent(image = R.drawable.ic_sweet_franky)
            Spacer(modifier = Modifier.height(24.dp))

            HeadingTextComponent("Verification Code")
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the 6-digit code sent to $maskedMobile",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OtpInput(
                otp = otp,
                onOtpChange = {
                    otp = it
                    viewModel.clearOtpError() // 👈 IMPORTANT
                    if (it.length == OTP_LENGTH) keyboardController?.hide()
                },
                focusRequester = focusRequester
            )

            Spacer(modifier = Modifier.height(8.dp))

            OtpErrorMessage(
                otpUiState = otpVerifyState
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ---------------- Resend Timer ----------------
            if (resendTime > 0) {
                Text(
                    text = "Resend OTP in $resendTime sec",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            } else {
                TextButton(onClick = { viewModel.resendOtp(mobile) }) {
                    Text("Resend OTP")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ---------------- Buttons ----------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.popBackStack() }
                ) {
                    Text(text="Cancel", color = Color.White,
                        fontSize = 18.sp)
                }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = isOtpComplete && otpVerifyState !is OtpUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = { viewModel.verifyOtp(mobile, otp) }
                ) {
                    Text(text = "Verify",
                        color = Color.White,
                        fontSize = 18.sp)
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
        // Hidden textfield for input
        BasicTextField(
            value = otp,
            onValueChange = { value ->
                if (value.length <= OTP_LENGTH && value.all(Char::isDigit)) {
                    onOtpChange(value)
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
            // Calculate box size dynamically
            val boxSize = ((LocalContext.current.resources.displayMetrics.widthPixels / LocalContext.current.resources.displayMetrics.density) - 48 /*padding*/ - 8 * (OTP_LENGTH - 1)) / OTP_LENGTH

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



//@Composable fun OtpInputRow( otpLength: Int = 5, onOtpComplete: (String) -> Unit ) { val otp = remember { mutableStateListOf(*Array(otpLength) { "" }) } val focusRequesters = remember { List(otpLength) { FocusRequester() } } Row( horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically ) { otp.forEachIndexed { index, value -> OtpDigitField( value = value, onValueChange = { newValue -> otp[index] = newValue if (newValue.isNotEmpty() && index < otpLength - 1) { focusRequesters[index + 1].requestFocus() } if (newValue.isEmpty() && index > 0) { focusRequesters[index - 1].requestFocus() } if (otp.all { it.isNotEmpty() }) { onOtpComplete(otp.joinToString("")) } }, focusRequester = focusRequesters[index], isLast = index == otpLength - 1 ) } } LaunchedEffect(Unit) { focusRequesters.first().requestFocus() } } @Composable private fun OtpDigitField( value: String, onValueChange: (String) -> Unit, focusRequester: FocusRequester, isLast: Boolean ) { OutlinedTextField( value = value, onValueChange = { input -> if (input.isEmpty()) { onValueChange("") } else if (input.last().isDigit()) { onValueChange(input.last().toString()) } }, modifier = Modifier .width(56.dp) .height(56.dp) .focusRequester(focusRequester), singleLine = true, textStyle = MaterialTheme.typography.titleLarge.copy( textAlign = TextAlign.Center ), keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.NumberPassword ), colors = TextFieldDefaults.colors( unfocusedIndicatorColor = MaterialTheme.colorScheme.primary, focusedIndicatorColor = Color.White, unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent, cursorColor = Color.White ) ) } @Composable fun ResendOtpTimer( totalTime: Int = 60, onResend: () -> Unit ) { var timeLeft by remember { mutableStateOf(totalTime) } LaunchedEffect(timeLeft) { if (timeLeft > 0) { delay(1000) timeLeft-- } } if (timeLeft > 0) { Text( text = "Resend OTP in $timeLeft sec", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f) ) } else { TextButton(onClick = { timeLeft = totalTime onResend() }) { Text("Resend OTP") } } } @Composable fun OtpVerificationScreen( navController: NavController, mobile: String ) { val maskedMobile = remember(mobile) { "*******${mobile.takeLast(2)}" } var isOtpComplete by remember { mutableStateOf(false) } var otpValue by remember { mutableStateOf("") } Surface( modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background ) { Column( modifier = Modifier .fillMaxSize() .padding(horizontal = 24.dp) ) { // 🔹 Top content Column( horizontalAlignment = Alignment.CenterHorizontally ) { Spacer(modifier = Modifier.height(32.dp)) ImageComponent(image = R.drawable.ic_sweet_franky) Spacer(modifier = Modifier.height(24.dp)) HeadingTextComponent(heading = "Verification Code") Spacer(modifier = Modifier.height(8.dp)) Text( text = "Enter the 5-digit code we've sent to $maskedMobile", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f) ) Spacer(modifier = Modifier.height(24.dp)) OtpInputRow( otpLength = 5, onOtpComplete = { otpValue = it isOtpComplete = true } ) Spacer(modifier = Modifier.height(16.dp)) ResendOtpTimer { // call resend OTP API } } Spacer(modifier = Modifier.weight(1f)) // 🔹 Bottom buttons Row( modifier = Modifier .fillMaxWidth() .padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp) ) { OutlinedButton( modifier = Modifier.weight(1f), onClick = { navController.popBackStack() } ) { Text("Cancel", color = Color.Gray,) } Button( modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors( containerColor = MaterialTheme.colorScheme.primary ), enabled = isOtpComplete, onClick = { // verify OTP using otpValue } ) { Text("Verify", color = Color.White,) } } } } }
