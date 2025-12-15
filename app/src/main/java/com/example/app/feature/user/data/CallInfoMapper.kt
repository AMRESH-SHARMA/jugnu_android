package com.example.app.feature.user.data

import com.example.app.feature.user.domain.CallerInfo

fun CallerInfoDto.toDomain(): CallerInfo {
    return CallerInfo(
        name = name,
        avatar = avatar
    )
}

