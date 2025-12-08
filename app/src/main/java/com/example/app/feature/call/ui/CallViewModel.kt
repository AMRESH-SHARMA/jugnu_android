//package com.example.app.feature.call.ui
//
//import androidx.lifecycle.ViewModel
//import com.example.app.feature.call.data.remote.model.CallData
//import com.example.app.feature.call.data.repository.CallRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject
//
//@HiltViewModel
//class CallViewModel @Inject constructor(
//    private val repo: CallRepository
//) : ViewModel() {
//
//    suspend fun startCall(caller: String, callee: String): CallData {
//        val channel = "channel-${System.currentTimeMillis()}"
//        return repo.startCall(caller, callee, channel).data
//    }
//
//    suspend fun accept(callId: String) =
//        repo.acceptCall(callId)
//
//    suspend fun reject(callId: String) =
//        repo.rejectCall(callId)
//}
