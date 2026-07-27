package com.zevclip.sender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zevclip.sender.filetransfer.FileTransferPeerPinStore

/**
 * Applies a pairing payload delivered over adb, e.g. after a reinstall wipes app data:
 *
 *   adb shell am broadcast -n com.zevclip.sender/.AdbPairingReceiver \
 *       -a com.zevclip.sender.ADB_PAIR --es payload '<pairing-json>'
 *
 * The manifest guards this receiver with android.permission.DUMP, which only
 * shell/system callers hold, so ordinary apps cannot inject pairing data.
 */
class AdbPairingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PAIR) return
        val rawValue = intent.getStringExtra(EXTRA_PAYLOAD).orEmpty()
        if (rawValue.isBlank()) {
            Log.w(TAG, "Pairing broadcast had no payload extra")
            return
        }

        PairingQrPayload.parse(rawValue)
            .onSuccess { payload ->
                ZevClipPreferences.saveEndpoint(context, payload.host, payload.port.toString())
                ZevClipPreferences.savePairingToken(context, payload.token)
                ZevClipPreferences.saveDeviceId(context, payload.deviceId)
                FileTransferPeerPinStore.savePinnedMacCertificateSha256(context, payload.transferCert)
                ZevClipPreferences.setClipboardSyncEnabled(context, true)
                AndroidClipboardReceiverService.start(context)
                Log.i(TAG, "Applied adb pairing payload for ${payload.name} at ${payload.host}:${payload.port}")
            }
            .onFailure { error ->
                Log.w(TAG, "Invalid adb pairing payload: ${error.message}")
            }
    }

    companion object {
        private const val TAG = "ZevClipAdbPairing"
        const val ACTION_PAIR = "com.zevclip.sender.ADB_PAIR"
        const val EXTRA_PAYLOAD = "payload"
    }
}
