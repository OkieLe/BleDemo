package io.github.okiele.btconnector.bt

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.os.Bundle
import io.github.okiele.btconnector.utils.BTConstants
import io.github.okiele.btconnector.utils.BTConstants.createChatService
import io.github.okiele.btconnector.utils.BTConstants.wrapMessage
import io.github.okiele.btconnector.utils.Gesture
import io.github.okiele.btconnector.utils.UNKNOWN
import io.github.boopited.droidbt.PeripheralManager
import io.github.boopited.droidbt.common.BaseManager
import io.github.boopited.droidbt.gatt.GattServer
import java.util.*

class GattServerManager(context: Context): BaseManager(context), GattServer.GattServerCallback {

    private var peripheralManager: PeripheralManager? = null
    private var bluetoothGattServer: GattServer = GattServer(context, this).apply { logEnabled = true }

    private val pendingMessage = mutableListOf<Gesture>()

    override fun onBluetoothEnabled(enable: Boolean) {
        super.onBluetoothEnabled(enable)
        if (!enable) {
            bluetoothGattServer.shutdown()
        }
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        if (peripheralManager?.isAdvertising() == true) {
            peripheralManager?.stop()
        }
    }

    override fun onDeviceDisconnected(device: BluetoothDevice) {
        if (peripheralManager?.isAdvertising() != true) {
            peripheralManager?.start()
        }
    }

    override fun isNotification(uuid: UUID): Boolean {
        return uuid == BTConstants.CONTENT_NOTIFY
    }

    override fun getCharacteristic(uuid: UUID): ByteArray? {
        return when (uuid) {
            BTConstants.CHARACTERISTIC_GESTURE -> {
                Log.i(TAG, "Read message")
                wrapMessage(pendingMessage.first())
            }
            else -> {
                // Invalid characteristic
                Log.w(TAG, "Invalid Characteristic Read: $uuid")
                null
            }
        }
    }

    override fun setCharacteristic(uuid: UUID, value: ByteArray): Boolean {
        return true
    }

    override fun getDescriptor(uuid: UUID): ByteArray? {
        return null
    }

    override fun setDescriptor(uuid: UUID, value: ByteArray): Boolean {
        return true
    }

    override fun start() {
        super.start()
        peripheralManager = PeripheralManager(context,
            mapOf(BTConstants.SERVICE_GESTURE.toString() to "".toByteArray())
        ).apply {
            logEnabled = true
            start()
        }
        if (bluetoothAdapter.isEnabled) {
            bluetoothGattServer.startService(createChatService())
        }
    }

    override fun stop() {
        if (bluetoothAdapter.isEnabled) {
            bluetoothGattServer.stopService(BTConstants.SERVICE_GESTURE)
        }
        bluetoothGattServer.shutdown()
        peripheralManager?.stop()
        super.stop()
    }

    /**
     * Send a chat service notification to any devices that are subscribed
     * to the characteristic.
     */
    fun notifyGesture(uuid: UUID, extra: Bundle?) {
        Log.i(TAG, "Sending update to subscribers")
        bluetoothGattServer.notifyDevices(
            BTConstants.SERVICE_GESTURE, uuid,
            wrapNotificationData(uuid, extra))
    }

    private fun wrapNotificationData(characteristic: UUID, extra: Bundle?): ByteArray {
        return when (characteristic) {
            BTConstants.CHARACTERISTIC_GESTURE -> {
                val time = extra?.getLong(BTConstants.EXTRA_TIME_STAMP) ?: System.currentTimeMillis()
                val data = extra?.getInt(BTConstants.EXTRA_DATA) ?: UNKNOWN
                wrapMessage(
                    Gesture(
                        data,
                        time
                    )
                )
            }
            else -> ByteArray(0)
        }
    }

    companion object {
        private const val TAG = "GattServerManager"
    }
}
