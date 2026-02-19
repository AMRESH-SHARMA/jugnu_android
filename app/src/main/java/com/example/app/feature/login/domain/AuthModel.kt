package com.example.app.feature.login.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class RequestOtpResult(
    val message: String
) : Parcelable

@Serializable
@Parcelize
data class VerifyOtpResult(
    val sessionId: String,
    val accountId: Long,
    val isNewUser: Boolean,
    val userRole: String,  // "CUSTOMER" or "LISTENER" from backend
    val isProfileComplete: Boolean = false
) : Parcelable