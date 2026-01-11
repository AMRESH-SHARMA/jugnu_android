package com.example.app.feature.login.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class RequestOtpResult(
    val success: Boolean,
    val message: String
) : Parcelable

@Serializable
@Parcelize
data class VerifyOtpResult(
    val token: String,
    val isNewUser: Boolean
) : Parcelable