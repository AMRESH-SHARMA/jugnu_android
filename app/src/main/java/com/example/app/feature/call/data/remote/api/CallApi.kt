//package com.example.app.feature.call.data.remote.api
//
//import com.example.app.feature.call.data.remote.model.StartCallRequest
//import com.example.app.feature.call.data.remote.model.StartCallResponse
//import retrofit2.http.Body
//import retrofit2.http.POST
//
//interface CallApi {
//
//    @POST("api/v1/call/start")
//    suspend fun startCall(
//        @Body body: StartCallRequest
//    ): StartCallResponse
//
//    @POST("api/v1/call/accept")
//    suspend fun acceptCall(
//        @Body body: Map<String, String>
//    ): StartCallResponse
//
//    @POST("api/v1/call/reject")
//    suspend fun rejectCall(
//        @Body body: Map<String, String>
//    ): StartCallResponse
//}
//
