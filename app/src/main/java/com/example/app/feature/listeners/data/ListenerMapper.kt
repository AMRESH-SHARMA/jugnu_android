package com.example.app.feature.listeners.data

import com.example.app.feature.listeners.domain.ListenerModel

fun ListenerDto.toDomain(): ListenerModel {
    return ListenerModel(
        id = publicId,
        name = name,
        avatar = avatar,
        tagLine = tagLine,
        about = about,
        age = age,
        gender = gender,
        experience = experience,
        rating = rating,
    )
}
