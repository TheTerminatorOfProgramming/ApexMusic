package com.ttop.app.apex.service

import android.content.AsyncQueryHandler
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.ttop.app.apex.libraries.appthemehelper.util.VersionUtils
import com.ttop.app.apex.util.PreferenceUtil

class AutoConnectionDetector(val context: Context) {

    companion object {
        const val TAG = "AutoConnectionDetector"

        // columnName for provider to query on connection status
        const val CAR_CONNECTION_STATE = "CarConnectionState"

        // auto app on your phone will send broadcast with this action when connection state changes
        const val ACTION_CAR_CONNECTION_UPDATED =
            "androidx.car.app.connection.action.CAR_CONNECTION_UPDATED"

        // phone is not connected to car
        const val CONNECTION_TYPE_NOT_CONNECTED = 0

        private const val QUERY_TOKEN = 42

        private const val CAR_CONNECTION_AUTHORITY = "androidx.car.app.connection"

        private val PROJECTION_HOST_URI =
            Uri.Builder().scheme("content").authority(CAR_CONNECTION_AUTHORITY).build()
    }

    private val carConnectionReceiver = CarConnectionBroadcastReceiver()
    private val carConnectionQueryHandler = CarConnectionQueryHandler(context.contentResolver)
    private var autoReceiverRegistered = false
    fun registerCarConnectionReceiver() {
        if (!autoReceiverRegistered) {
            if (VersionUtils.hasT()) {
                context.registerReceiver(
                    carConnectionReceiver,
                    IntentFilter(ACTION_CAR_CONNECTION_UPDATED),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(
                    carConnectionReceiver,
                    IntentFilter(ACTION_CAR_CONNECTION_UPDATED)
                )
            }
            autoReceiverRegistered = true
        }

        queryForState()
    }

    fun unRegisterCarConnectionReceiver() {
        if (autoReceiverRegistered) {
            context.unregisterReceiver(carConnectionReceiver)
            autoReceiverRegistered = false
        }
    }

    private fun queryForState() {
        carConnectionQueryHandler.startQuery(
            QUERY_TOKEN,
            null,
            PROJECTION_HOST_URI,
            arrayOf(CAR_CONNECTION_STATE),
            null,
            null,
            null
        )
    }

    inner class CarConnectionBroadcastReceiver : BroadcastReceiver() {
        // query for connection state every time the receiver receives the broadcast
        override fun onReceive(context: Context?, intent: Intent?) {
            queryForState()
        }
    }

    internal class CarConnectionQueryHandler(resolver: ContentResolver?) :
        AsyncQueryHandler(resolver) {
        // notify new queryed connection status when query complete
        override fun onQueryComplete(token: Int, cookie: Any?, response: Cursor?) {
            if (response == null) {
                Log.w(
                    TAG,
                    "Null response from content provider when checking connection to the car, treating as disconnected"
                )
                PreferenceUtil.isCarConnected = false
                return
            }
            val carConnectionTypeColumn = response.getColumnIndex(CAR_CONNECTION_STATE)
            if (carConnectionTypeColumn < 0) {
                Log.w(
                    TAG,
                    "Connection to car response is missing the connection type, treating as disconnected"
                )
                PreferenceUtil.isCarConnected = false
                return
            }
            if (!response.moveToNext()) {
                Log.w(TAG, "Connection to car response is empty, treating as disconnected")
                PreferenceUtil.isCarConnected = false
                return
            }
            val connectionState = response.getInt(carConnectionTypeColumn)
            if (connectionState == CONNECTION_TYPE_NOT_CONNECTED) {
                Log.i(TAG, "Android Auto disconnected")
                PreferenceUtil.isCarConnected = false
            } else {
                Log.i(TAG, "Android Auto connected")
                PreferenceUtil.isCarConnected = true
            }
        }
    }
}