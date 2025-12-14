object Routes {

    object Graph {
        const val SELECT_USER_ROLE = "select_user_role_graph"
        const val HOME = "home_graph"
        const val CHAT = "chat_graph"
        const val WALLET = "wallet_graph"
        const val CALL = "call_graph"
        const val AUTH = "auth_graph"
    }

    object Screen {

        object SelectUserRole {
            const val ROOT = "select_user_role"
        }

        object Home {
            const val ROOT = "home"
        }

        object Chat {
            const val ROOT = "chat"
        }

        object Wallet {
            const val ROOT = "wallet"
        }

        object Call {
            const val ROOT = "call"
            const val INCOMING = "incoming_call"
            const val ONGOING = "ongoing_call"
        }
    }
}
