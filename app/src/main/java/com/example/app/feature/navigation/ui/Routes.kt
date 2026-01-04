object Routes {

    object Graph {
        const val SELECT_USER_ROLE = "select_user_role_graph"
        const val HOME = "home_graph"
        const val LISTENER = "listener_graph"
        const val CHAT = "chat_graph"
        const val WALLET = "wallet_graph"
        const val CALL = "call_graph"
        const val AUTH = "auth_graph"
    }

    object Screen {

        // 🔐 AUTH
        object Auth {
            const val LOGIN = "login"
            const val OTP = "otp/{mobile}"

            fun otpRoute(mobile: String) = "otp/$mobile"
        }

        object SelectUserRole {
            const val ROOT = "select_user_role"
        }

        object Home {
            const val ROOT = "home"
        }

        object Listener {
            const val ROOT = "listener"
            const val ListenerDashboard = "listener_dashboard"
        }


        object Chat {
            const val ROOT = "chat"
        }

        object Wallet {
            const val ROOT = "wallet"
            const val ENTER_AMOUNT = "wallet_enter_amount"
        }

        object Call {
            const val ROOT = "call"
            const val INCOMING = "incoming_call"
            const val ONGOING = "ongoing_call"
        }
    }
}
