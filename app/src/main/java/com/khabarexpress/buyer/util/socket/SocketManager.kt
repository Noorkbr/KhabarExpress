package com.khabarexpress.buyer.util.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Socket.IO Manager for real-time updates
 * Handles connection lifecycle and event listening for:
 * - order:status_update
 * - rider:location_update
 */
@Singleton
class SocketManager @Inject constructor() {
    
    private var socket: Socket? = null
    private val tag = "SocketManager"
    
    // Event flows
    private val _orderStatusUpdates = MutableSharedFlow<OrderStatusUpdate>()
    val orderStatusUpdates: SharedFlow<OrderStatusUpdate> = _orderStatusUpdates.asSharedFlow()
    
    private val _riderLocationUpdates = MutableSharedFlow<RiderLocationUpdate>()
    val riderLocationUpdates: SharedFlow<RiderLocationUpdate> = _riderLocationUpdates.asSharedFlow()
    
    private val _connectionState = MutableSharedFlow<ConnectionState>(replay = 1)
    val connectionState: SharedFlow<ConnectionState> = _connectionState.asSharedFlow()
    
    /**
     * Initialize and connect to Socket.IO server
     */
    fun connect(serverUrl: String, token: String? = null) {
        try {
            val options = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                token?.let {
                    auth = mapOf("token" to it)
                }
            }
            
            socket = IO.socket(serverUrl, options)
            
            socket?.apply {
                on(Socket.EVENT_CONNECT, onConnect)
                on(Socket.EVENT_DISCONNECT, onDisconnect)
                on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                on("order:status_update", onOrderStatusUpdate)
                on("rider:location_update", onRiderLocationUpdate)
                
                connect()
            }
            
            Log.d(tag, "Connecting to socket server: $serverUrl")
            
        } catch (e: URISyntaxException) {
            Log.e(tag, "Failed to create socket", e)
            emitConnectionState(ConnectionState.Error(e.message ?: "Connection failed"))
        }
    }
    
    /**
     * Disconnect from Socket.IO server
     */
    fun disconnect() {
        socket?.apply {
            off()
            disconnect()
        }
        socket = null
        Log.d(tag, "Socket disconnected")
        emitConnectionState(ConnectionState.Disconnected)
    }
    
    /**
     * Join a specific order room for updates
     */
    fun joinOrderRoom(orderId: String) {
        socket?.emit("join:order", JSONObject().apply {
            put("orderId", orderId)
        })
        Log.d(tag, "Joined order room: $orderId")
    }
    
    /**
     * Leave an order room
     */
    fun leaveOrderRoom(orderId: String) {
        socket?.emit("leave:order", JSONObject().apply {
            put("orderId", orderId)
        })
        Log.d(tag, "Left order room: $orderId")
    }
    
    private val onConnect = Emitter.Listener {
        Log.d(tag, "Socket connected")
        emitConnectionState(ConnectionState.Connected)
    }
    
    private val onDisconnect = Emitter.Listener {
        Log.d(tag, "Socket disconnected")
        emitConnectionState(ConnectionState.Disconnected)
    }
    
    private val onConnectError = Emitter.Listener { args ->
        val error = args.firstOrNull()
        Log.e(tag, "Socket connection error: $error")
        emitConnectionState(ConnectionState.Error(error?.toString() ?: "Unknown error"))
    }
    
    private val onOrderStatusUpdate = Emitter.Listener { args ->
        try {
            val data = args.firstOrNull() as? JSONObject ?: return@Listener
            val orderId = data.optString("orderId")
            val status = data.optString("status")
            val timestamp = data.optLong("timestamp")
            val message = data.optString("message")
            
            val update = OrderStatusUpdate(
                orderId = orderId,
                status = status,
                timestamp = timestamp,
                message = message
            )
            
            Log.d(tag, "Order status update: $update")
            emitOrderStatusUpdate(update)
            
        } catch (e: Exception) {
            Log.e(tag, "Error parsing order status update", e)
        }
    }
    
    private val onRiderLocationUpdate = Emitter.Listener { args ->
        try {
            val data = args.firstOrNull() as? JSONObject ?: return@Listener
            val riderId = data.optString("riderId")
            val orderId = data.optString("orderId")
            val latitude = data.optDouble("latitude")
            val longitude = data.optDouble("longitude")
            val timestamp = data.optLong("timestamp")
            
            val update = RiderLocationUpdate(
                riderId = riderId,
                orderId = orderId,
                latitude = latitude,
                longitude = longitude,
                timestamp = timestamp
            )
            
            Log.d(tag, "Rider location update: $update")
            emitRiderLocationUpdate(update)
            
        } catch (e: Exception) {
            Log.e(tag, "Error parsing rider location update", e)
        }
    }
    
    private fun emitConnectionState(state: ConnectionState) {
        _connectionState.tryEmit(state)
    }
    
    private fun emitOrderStatusUpdate(update: OrderStatusUpdate) {
        _orderStatusUpdates.tryEmit(update)
    }
    
    private fun emitRiderLocationUpdate(update: RiderLocationUpdate) {
        _riderLocationUpdates.tryEmit(update)
    }
    
    /**
     * Check if socket is currently connected
     */
    fun isConnected(): Boolean = socket?.connected() == true
}

// Data classes for events
data class OrderStatusUpdate(
    val orderId: String,
    val status: String,
    val timestamp: Long,
    val message: String?
)

data class RiderLocationUpdate(
    val riderId: String,
    val orderId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

// Connection state
sealed class ConnectionState {
    data object Connected : ConnectionState()
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
