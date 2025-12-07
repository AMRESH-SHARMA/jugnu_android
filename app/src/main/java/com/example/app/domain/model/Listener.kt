package com.example.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Listener(
    val id: Int,
    val avatar: String?,
    val name: String,
    val bio: String?,
    val rating: Double?
) : Parcelable
