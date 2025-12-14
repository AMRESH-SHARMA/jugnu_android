package com.example.app.core.rtm
//
//import com.example.app.core.network.data.ApiRepository
//import com.example.app.core.session.SessionManager
//
//object PresenceManager {
//
//    suspend fun setOnline() {
//        val id = SessionManager.userId
//        if (id > 0) {
//            ApiRepository.setOnline(id)
//        }
//    }
//
//    suspend fun setOffline() {
//        val id = SessionManager.userId
//        if (id > 0) {
//            ApiRepository.setOffline(id)
//        }
//    }
//}
