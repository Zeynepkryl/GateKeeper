package com.zeynep.gatekeeper.domain.model

enum class ConnectionState(val displayName: String) {
    Disconnected("Disconnected"),
    Connecting("Connecting..."),
    Ready("Ready"),
    Error("Error")
}
