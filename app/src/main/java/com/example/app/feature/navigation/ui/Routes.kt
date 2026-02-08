object Routes {

    object Graph {
        const val AUTH = "auth_graph"
        const val HOME = "home_graph"
        const val LISTENER = "listener_graph"
        const val CHAT = "chat_graph"
        const val WALLET = "wallet_graph"
        const val CALL = "call_graph"
    }

    object Screen {

        // 🔐 AUTH
        object Auth {
            const val LOGIN = "login"
            const val OTP = "otp/{mobile}"
            const val PROFILE_SETUP = "profile_setup"

            fun otpRoute(mobile: String) = "otp/$mobile"
        }

        // 🏠 HOME (Customer)
        object Home {
            const val ROOT = "home"
            const val OFFER_MODAL = "offer_modal"
        }

        // 👂 LISTENER
        object Listener {
            const val ROOT = "listener"
            const val DASHBOARD = "listener_dashboard"
        }

        // 💬 CHAT
        object Chat {
            const val ROOT = "chat/{listenerId}"
            
            fun chatRoute(listenerId: Long) = "chat/$listenerId"
        }

        // 💰 WALLET
        object Wallet {
            const val ROOT = "wallet"
            const val ENTER_AMOUNT = "wallet_enter_amount/{flowType}"
            
            fun enterAmountRoute(flowType: String) = "wallet_enter_amount/$flowType"
        }

        // 📞 CALL
        object Call {
            const val ROOT = "call"
            const val INCOMING = "incoming_call"
            const val ONGOING = "ongoing_call"
        }
    }
}
