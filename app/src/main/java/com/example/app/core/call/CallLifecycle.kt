package com.example.app.core.call

import android.util.Log
import com.example.app.feature.call.domain.CallStatus

/**
 * CallLifecycle manages call state transitions and validation.
 * Single source of truth for the call state machine.
 */
object CallLifecycle {

    private const val TAG = "RTM"

    // Define allowed state transitions
    private val allowedTransitions = mapOf(
        // Incoming call flow
        CallStatus.INCOMING_RINGING to setOf(
            CallStatus.CONNECTING,
            CallStatus.REJECTED,
            CallStatus.CANCELLED,
            CallStatus.ENDED
        ),

        // Outgoing call flow
        CallStatus.OUTGOING_CONNECTING to setOf(
            CallStatus.OUTGOING_RINGING,
            CallStatus.CANCELLED,
            CallStatus.REJECTED,
            CallStatus.ENDED
        ),
        CallStatus.OUTGOING_RINGING to setOf(
            CallStatus.CONNECTING,
            CallStatus.CANCELLED,
            CallStatus.REJECTED,
            CallStatus.ENDED
        ),

        // Connected flow
        CallStatus.CONNECTING to setOf(
            CallStatus.CONNECTED,
            CallStatus.ENDED
        ),
        CallStatus.CONNECTED to setOf(
            CallStatus.ENDED
        )

        // Terminal states (ENDED, REJECTED, CANCELLED) have no outgoing transitions
    )

    /**
     * Validates if a state transition is allowed
     */
    fun validateTransition(from: CallStatus, to: CallStatus): Result<Unit> {
        // Allow idempotent operations (same state)
        if (from == to) {
            return Result.success(Unit)
        }

        val allowed = allowedTransitions[from]?.contains(to) ?: false
        return if (allowed) {
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalStateException("Invalid call state transition: $from → $to")
            )
        }
    }

    /**
     * Checks if a transition is valid without throwing
     */
    fun canTransition(from: CallStatus, to: CallStatus): Boolean {
        if (from == to) return true
        return allowedTransitions[from]?.contains(to) ?: false
    }

    /**
     * Attempts to transition to a new state with validation and logging
     */
    fun transitionTo(
        currentStatus: CallStatus,
        newStatus: CallStatus,
        callId: String
    ): Result<Unit> {
        val result = validateTransition(currentStatus, newStatus)
        
        if (result.isSuccess) {
            Log.d(TAG, "State transition: $currentStatus → $newStatus (callId=$callId)")
        } else {
            Log.w(TAG, "Invalid transition blocked: $currentStatus → $newStatus (callId=$callId)")
        }
        
        return result
    }

    /**
     * Checks if a status is a terminal state (no further transitions allowed)
     */
    fun isTerminalState(status: CallStatus): Boolean {
        return status in setOf(
            CallStatus.ENDED,
            CallStatus.REJECTED,
            CallStatus.CANCELLED
        )
    }

    /**
     * Checks if a status represents an active call
     */
    fun isActiveCall(status: CallStatus): Boolean {
        return status in setOf(
            CallStatus.INCOMING_RINGING,
            CallStatus.OUTGOING_CONNECTING,
            CallStatus.OUTGOING_RINGING,
            CallStatus.CONNECTING,
            CallStatus.CONNECTED
        )
    }
}
