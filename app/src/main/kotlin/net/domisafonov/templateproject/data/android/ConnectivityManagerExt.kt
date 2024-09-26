package net.domisafonov.templateproject.data.android

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import net.domisafonov.templateproject.ui.STARTUP_NETWORK_AVAILABILITY_DELAY
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

fun ConnectivityManager.isOffline(): Flow<Boolean> = callbackFlow {
    val hadAvailabilityEvent = AtomicBoolean(false)
    val callback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            hadAvailabilityEvent.set(true)
            Timber.v("network connected")
            channel.trySend(false)
        }

        override fun onLost(network: Network) {
            hadAvailabilityEvent.set(true)
            Timber.v("network disconnected")
            channel.trySend(true)
        }
    }
    registerDefaultNetworkCallback(callback)
    delay(STARTUP_NETWORK_AVAILABILITY_DELAY)
    if (!hadAvailabilityEvent.get()) {
        channel.trySend(true)
    }
    awaitClose { unregisterNetworkCallback(callback) }
}.conflate()
