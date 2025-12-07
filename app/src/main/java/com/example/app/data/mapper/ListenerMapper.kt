package com.example.app.data.mapper

import com.example.app.data.remote.model.ListenerDto
import com.example.app.domain.model.Listener

fun ListenerDto.toDomain(): Listener {
    return Listener(
        id = id,
        avatar = avatar,
        name = about ?: "Unknown",   // temporary
        bio = bio,
        rating = rating,
    )
}
