package com.example.app.core.preferences.user.domain

enum class UserRole {
    CUSTOMER,
    LISTENER;

    companion object {
        fun fromString(value: String?): UserRole =
            entries.firstOrNull { it.name == value } ?: CUSTOMER
    }
}