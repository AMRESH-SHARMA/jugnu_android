package com.example.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.app.core.call.CallEvent
import com.example.app.core.call.CallEventBus
import com.example.app.core.call.PendingCallStore
import com.example.app.core.payment.UpiPaymentHandler
import com.example.app.core.session.SessionManager
import com.example.app.feature.theme.AppTheme
import com.example.app.feature.wallet.ui.EnterAmountViewModel
import com.example.app.root.AppRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var pendingCallStore: PendingCallStore

    private val enterAmountViewModel: EnterAmountViewModel by viewModels()

    private lateinit var upiLauncher: ActivityResultLauncher<Intent>
    private lateinit var upiHandler: UpiPaymentHandler

    // 🔐 txnRef generated BEFORE UPI launch
    private var pendingTxnRef: String? = null

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1️⃣ Initialize UPI result handler
        upiHandler = UpiPaymentHandler(
            context = this,
            onUpiResult = { status, txnId, returnedTxnRef, approvalRefNo, responseCode, rawResponse ->

                if (status == "cancelled" || status == "failure") {
                    enterAmountViewModel.onUpiCancelled()
                    return@UpiPaymentHandler
                }

                val finalTxnRef =
                    returnedTxnRef?.takeIf { it.isNotBlank() } ?: pendingTxnRef

                verifyPaymentWithBackend(
                    status = status,
                    txnId = txnId,
                    txnRef = finalTxnRef,
                    approvalRefNo = approvalRefNo,
                    responseCode = responseCode,
                    rawResponse = rawResponse
                )
            }
        )

        // 2️⃣ Activity Result launcher
        upiLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d("UPI_DEBUG", "UPI result received: $result")

                if (result.data == null) {
                    enterAmountViewModel.onUpiCancelled()
                    return@registerForActivityResult
                }

                upiHandler.handleResult(result)
            }

        // 3️⃣ Observe ViewModel → launch UPI
        lifecycleScope.launch {
            enterAmountViewModel.upiEvent.collect { amountInPaise ->

                pendingTxnRef = "TXN_${System.currentTimeMillis()}"

                launchUpi(
                    amountInPaise = amountInPaise,
                    txnRef = pendingTxnRef!!
                )
            }
        }

        restorePendingIncomingCall()

        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }

    // 🔑 Activity-only UPI launch
    private fun launchUpi(amountInPaise: Long, txnRef: String) {
        val vpa = "9450776075@ptyes" // ⚠️ test VPA
        val appName = getString(R.string.app_name)

        // Convert paise → rupees with 2 decimals
        val amountRupees = "%.2f".format(amountInPaise / 100.0)

        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", vpa)
            .appendQueryParameter("pn", appName)
            .appendQueryParameter("tr", txnRef)
            .appendQueryParameter("tn", "Wallet Topup")
            .appendQueryParameter("am", amountRupees)
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)

        // Optional: lock to Google Pay
        // intent.setPackage("com.google.android.apps.nbu.paisa.user")

        val chooser = Intent.createChooser(intent, "Pay with UPI")

        try {
            upiLauncher.launch(chooser)
        } catch (e: Exception) {
            Log.e("UPI_DEBUG", "No UPI app found", e)
            enterAmountViewModel.onUpiCancelled()
        }
    }

    /**
     * 🔒 Backend verification entry point
     * Client MUST NOT decide final payment state
     */
    private fun verifyPaymentWithBackend(
        status: String,
        txnId: String?,
        txnRef: String?,
        approvalRefNo: String?,
        responseCode: String?,
        rawResponse: String
    ) {
        Log.d(
            "UPI_DEBUG",
            "Verify: status=$status txnId=$txnId txnRef=$txnRef approvalRefNo=$approvalRefNo"
        )

        // Call backend here (intentionally commented)
    }

    private fun restorePendingIncomingCall() {
        lifecycleScope.launch {
            val pending = pendingCallStore.consume() ?: return@launch

            CallEventBus.emit(
                CallEvent.Incoming(
                    callId = pending.callId,
                    callType = pending.callType,
                    callerAccountId = pending.callerAccountId,
                    calleeAccountId = SessionManager.userId,
                    channel = null
                )
            )
        }
    }
}


/*
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var pendingCallStore: PendingCallStore

    private val enterAmountViewModel: EnterAmountViewModel by viewModels()

    private lateinit var upiLauncher: ActivityResultLauncher<Intent>

    private lateinit var upiHandler: UpiPaymentHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1️⃣ Initialize UPI handler
        upiHandler = UpiPaymentHandler(
            context = this,
            onUpiResult = { status, txnId, txnRef, approvalRefNo, responseCode, rawResponse ->

                if (status == "cancelled" || status == "failure") {
                    enterAmountViewModel.onUpiCancelled()
                    return@UpiPaymentHandler
                }

//                if (rawResponse.isNullOrBlank()) {
//                    enterAmountViewModel.onUpiCancelled()
//                    return
//                }

                verifyPaymentWithBackend(
                    status = status,
                    txnId = txnId,
                    txnRef = txnRef,
                    approvalRefNo = approvalRefNo,
                    responseCode = responseCode,
                    rawResponse = rawResponse
                )
            }
        )

        // 2️⃣ Register UPI Activity Result launcher
        upiLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d("UPI_DEBUG", "MainActivity: UPI result callback fired $result")
                upiHandler.handleResult(result)
            }

        // 3️⃣ Observe UPI trigger from ViewModel
        Log.d("UPI_DEBUG", "MainActivity: setting up UPI event collector")
        lifecycleScope.launch {
            Log.d("UPI_DEBUG", "MainActivity: UPI collector coroutine started")
            enterAmountViewModel.upiEvent.collect { amount ->
                Log.d("UPI_DEBUG", "MainActivity: received UPI event amount=$amount")
                upiHandler.launchUpi(
                    launcher = upiLauncher,
                    amount = amount,
                    vpa = "9450776075@ptyes", // ⚠️ test VPA only
                    appName = getString(R.string.app_name)
                )
            }
        }

        // Existing logic (unchanged)
        restorePendingIncomingCall()

        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }

    /**
     * 🔒 Backend verification entry point
     * Client MUST NOT decide final payment status
     */
    private fun verifyPaymentWithBackend(
        status: String,
        txnId: String?,
        txnRef: String?,
        approvalRefNo: String?,
        responseCode: String?,
        rawResponse: String
    ) {
        Log.d(
            "RTM",
            "UPI_DEBUG Verify payment: status=$status txnId=$txnId txnRef=$txnRef approvalRefNo=$approvalRefNo"
        )
        /*
            lifecycleScope.launch {
        val result = paymentRepository.verifyUpiPayment(
            status = status,
            txnId = txnId,
            txnRef = txnRef,
            approvalRefNo = approvalRefNo,
            responseCode = responseCode,
            rawResponse = rawResponse
        )

        // 🔑 STEP 3 happens HERE
        enterAmountViewModel.onUpiFlowFinished()

        when (result) {
            is ApiResult.Success -> {
                // show success UI / navigate
            }
            is ApiResult.Error -> {
                // show error
            }
        }
    }
        * */
        // TODO:
        // Call ViewModel / Repository
        // Backend verifies with PSP / bank
        // Credit wallet ONLY after backend confirms
    }

    private fun restorePendingIncomingCall() {
        lifecycleScope.launch {
            val pending = pendingCallStore.consume() ?: return@launch

            CallEventBus.emit(
                CallEvent.Incoming(
                    callId = pending.callId,
                    callType = pending.callType,
                    callerAccountId = pending.callerAccountId,
                    calleeAccountId = SessionManager.userId,
                    channel = null
                )
            )
        }
    }
}


 */