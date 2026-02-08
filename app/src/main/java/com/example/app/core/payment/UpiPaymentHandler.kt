package com.example.app.core.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri


class UpiPaymentHandler(
    private val context: Context,
    private val onUpiResult: (
        status: String,
        txnId: String?,
        txnRef: String?,
        approvalRefNo: String?,
        responseCode: String?,
        rawResponse: String
    ) -> Unit
) {

    fun launchUpi(
        launcher: ActivityResultLauncher<Intent>,
        amount: Long,
        vpa: String,
        appName: String
    ) {
        Log.d("UPI_DEBUG", "UpiPaymentHandler.launchUpi amount=$amount")

        val txnRef = "TXN_${System.currentTimeMillis()}"
//        val uri = ("upi://pay" +
//                "?pa=$vpa" +
//                "&pn=${Uri.encode(appName)}" +
//                "&tr=$txnRef" +
//                "&tn=Wallet Topup" +
//                "&am=$amount" +
//                "&cu=INR").toUri()

        val uri = ("upi://pay" +
                "?pa=$vpa" +
                "&pn=${Uri.encode(appName)}" +
                "&tn=Wallet Topup" +
                "&am=$amount" +
                "&cu=INR").toUri()

        try {
//            upiLauncher.launch(chooser)
            launcher.launch(Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), "Pay with UPI"))
        } catch (e: Exception) {
            Log.e("UPI_DEBUG", "No UPI app found", e)
        }

    }

    fun handleResult(result: ActivityResult) {
        val rawResponse = result.data?.getStringExtra("response") ?: return
        Log.d("UPI_DEBUG", "handleResult rawResponse=$rawResponse")
        val parsed = parseResponse(rawResponse)
        val status = parsed["status"]?.lowercase() ?: "pending"

        onUpiResult(
            status,
            parsed["txnid"],
            parsed["txnref"],
            parsed["approvalrefno"],
            parsed["responsecode"],
            rawResponse
        )
    }

    private fun parseResponse(response: String): Map<String, String> {
        return response.split("&")
            .mapNotNull {
                val p = it.split("=")
                if (p.size == 2) p[0].lowercase() to p[1] else null
            }.toMap()
    }
}
