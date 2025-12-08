//package com.example.app.feature.call.data.repository
//
//import com.example.app.feature.call.data.remote.api.CallApi
//import com.example.app.feature.call.data.remote.model.StartCallRequest
//import javax.inject.Inject
//
//
//class CallRepository @Inject constructor(
//    private val api: CallApi
//) {
//    suspend fun startCall(caller: String, callee: String, channel: String) =
//        api.startCall(
//            StartCallRequest(
//                callerId = caller,
//                calleeId = callee,
//                channel = channel
//            )
//        )
//
//    suspend fun acceptCall(callId: String) =
//        api.acceptCall(mapOf("callId" to callId))
//
//    suspend fun rejectCall(callId: String) =
//        api.rejectCall(mapOf("callId" to callId))
//}
//
