//package com.example.app.core.network.repository
//
//import com.example.app.core.network.api.CallApiService
//import com.example.app.core.network.data.NotifyFcmCallRequest
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class ApiRepository @Inject constructor(
//    private val callApi: CallApiService
//) {
//    suspend fun notifyCallViaFcm(callId: String, callerId: Long, calleeId: Long) {
//        callApi.notifyCallViaFcm(
//            NotifyFcmCallRequest(
//                callId = callId,
//                callerId = callerId,
//                calleeId = calleeId
//            )
//        )
//    }
//}
