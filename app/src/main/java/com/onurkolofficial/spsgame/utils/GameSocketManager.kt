package com.onurkolofficial.spsgame.utils

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class GameSocketManager {
    private var socket: Socket? = null

    private val _onlinePlayerCount = MutableStateFlow<Int?>(null)
    val onlinePlayerCount: StateFlow<Int?> = _onlinePlayerCount.asStateFlow()

    fun getSocket(): Socket {
        if (socket == null) {
            val opts = IO.Options().apply {
                forceNew = false
                reconnection = true
            }
            socket = IO.socket(GameAppConfig.SOCKET_URL, opts).apply {
                on(Socket.EVENT_CONNECT) {
                    emit("request_player_count")
                }
                on("player_count") { args ->
                    if (args.isNotEmpty()) {
                        val data = args[0] as? JSONObject
                        val count = data?.optInt("count", 0) ?: 0
                        _onlinePlayerCount.value = count
                    }
                }
            }
        }
        return socket!!
    }

    fun connect() {
        try {
            val s = getSocket()
            if (!s.connected()) {
                s.connect()
            } else {
                s.emit("request_player_count")
            }
        } catch (e: Exception) {
            Log.e("GameSocketManager", "Error connecting socket", e)
        }
    }

    fun disconnect() {
        try {
            socket?.disconnect()
            socket = null
        } catch (e: Exception) {
            Log.e("GameSocketManager", "Error disconnecting socket", e)
        }
    }
}
