package com.example.app.feature.listeners.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class ListenerModel(
    val id: String,
    val name: String,
    val avatar: String?,
    val tagLine: String?,
    val about: String?,
    val age: Int,
    val gender: String,
    val experience: Int,
    val rating: Double,
) : Parcelable